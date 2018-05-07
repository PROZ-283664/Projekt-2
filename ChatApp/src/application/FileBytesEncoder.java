package application;

import java.nio.ByteBuffer;

public class FileBytesEncoder {
	static public ByteBuffer encode(FileBytes file) {
		return (ByteBuffer) file.toBinary().flip();
	}
}
