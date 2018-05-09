package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileHandler {
	final static int chunkSize = 1000000;
	private final String fileName;
	private final Integer uniqueID;
	private File file;
	private RandomAccessFile rs;
	private Boolean isComplete;
	private FileInputStream is;
	private long bytesToProcess;
	private long originalFileSize;
	private Boolean canDownload;

	FileHandler(File file, Integer uID) {
		fileName = file.getName();
		this.file = file;
		uniqueID = uID;
		isComplete = true;
		originalFileSize = bytesToProcess = file.length();
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	FileHandler(FileMessage message) {
		fileName = message.getFileName();
		isComplete = false;
		originalFileSize = bytesToProcess = message.getFileSize();
		uniqueID = message.getUID();
		canDownload = true;
		try {
			file = Files.createTempFile("tempfiles", ".tmp").toFile();
			file.deleteOnExit();

			rs = new RandomAccessFile(file, "rw");
			rs.setLength(originalFileSize);
			rs.seek(0);
		} catch (IOException e) {
			canDownload = false;
			try {
				rs.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public Boolean joinMessage(FileBytes fileChunk) {
		if (isComplete)
			return isComplete;
		try {
			ByteBuffer temp = fileChunk.getFileBytes();
			byte[] array = new byte[temp.capacity()];
			temp.get(array);
			rs.write(array);
			bytesToProcess -= array.length;
			isComplete = fileChunk.isLast();
			if (isComplete) {
				rs.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isComplete;
	}

	public Boolean saveFile(Path dest) {
		try {
			Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public String getFileName() {
		return fileName;
	}

	public ByteBuffer getNextChunk() {
		int tempChunkSize = (int) (bytesToProcess > chunkSize ? chunkSize : bytesToProcess);
		ByteBuffer temp = ByteBuffer.allocate(tempChunkSize);
		byte[] chunk = new byte[tempChunkSize];
		try {
			if (bytesToProcess > 0 && is.read(chunk) != -1) {
				bytesToProcess -= tempChunkSize;
				return temp.put(chunk);
			} else {
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Integer getHash() {
		return uniqueID;
	}

	public float dataProcessingRatio() {
		return (float) (originalFileSize - bytesToProcess) / (float) originalFileSize;
	}

	public Boolean canDownload() {
		return canDownload;
	}

	public long getFileSize() {
		return originalFileSize;
	}
}
