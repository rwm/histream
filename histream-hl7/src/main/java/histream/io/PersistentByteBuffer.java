package histream.io;

import java.nio.ByteBuffer;

public interface PersistentByteBuffer {
	public ByteBuffer getBuffer();
	public ByteBuffer enlargeBuffer();
	public void initBuffer();
}
