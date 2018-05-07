package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class FileHandler {
	final static int chunkSize = 1000000;
	private final String fileName;
	private final Integer uniqueID;
	private File file;
	private Boolean isComplete;
	private FileInputStream is;
	private long bytesToSend;

	FileHandler(File file, Integer uID) {
		fileName = file.getName();
		this.file = file;
		uniqueID = uID;
		isComplete = true;
		bytesToSend = file.length();
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	FileHandler(FileMessage message) {
		fileName = message.getFileName();
		isComplete = false;
		bytesToSend = 0;
		uniqueID = message.getUID();
		try {
			file = Files.createTempFile("tempfiles", ".tmp").toFile();
			is = new FileInputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Boolean joinMessage(FileBytes fileChunk) {
		if (isComplete)
			return isComplete;
		try {
			ByteBuffer temp = fileChunk.getFileBytes();
			byte[] array = new byte[temp.capacity()];
			temp.get(array);
			Files.write(file.toPath(), array, StandardOpenOption.APPEND);
			isComplete = fileChunk.isLast();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileChunk.isLast();
	}

	public void saveFile(Path dest) {
		try {
			Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFileName() {
		return fileName;
	}

	public ByteBuffer getNextChunk() {
		int tempChunkSize = (int) (bytesToSend > chunkSize ? chunkSize : bytesToSend);
		ByteBuffer temp = ByteBuffer.allocate(tempChunkSize);
		byte[] chunk = new byte[tempChunkSize];
		try {
			if (bytesToSend > 0 && is.read(chunk) != -1) {
				bytesToSend -= tempChunkSize;
				return temp.put(chunk);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Integer getHash() {
		return uniqueID;
	}
}
