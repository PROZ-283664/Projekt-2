package application;

import java.nio.ByteBuffer;

public class FileBytes {
	private final Integer uID;
	private final ByteBuffer fileBytes;
	private final Boolean isLast;

	FileBytes(Integer uID, ByteBuffer fileBytes, Boolean isLast) {
		this.uID = uID;
		this.fileBytes = fileBytes;
		this.isLast = isLast;
	}

	public Boolean isLast() {
		return isLast;
	}

	public Integer getUID() {
		return uID;
	}

	public ByteBuffer getFileBytes() {
		return fileBytes;
	}

	public int capacity() {
		return fileBytes.capacity();
	}

	public ByteBuffer toBinary() {
		byte last = (byte) (isLast ? 1 : 0);
		ByteBuffer temp = ByteBuffer.allocate(4 + fileBytes.capacity() + 1);
		return temp.putInt(uID).put((ByteBuffer) fileBytes.flip()).put(last);
	}

	public void clear() {
		fileBytes.clear();
	}
}
