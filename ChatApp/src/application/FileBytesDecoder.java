package application;

import java.nio.ByteBuffer;

public class FileBytesDecoder {

	static public FileBytes decode(ByteBuffer file) {
		byte[] bfileBytes = new byte[file.capacity() - 5];

		Integer uID = file.getInt();
		file.get(bfileBytes);
		byte bisLast = file.get();

		ByteBuffer fileBytes = ByteBuffer.wrap(bfileBytes);

		Boolean isLast = bisLast == (byte) 1;

		return new FileBytes(uID, fileBytes, isLast);
	}

}
