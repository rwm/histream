package histream.io;

import java.nio.ByteBuffer;

public class MLLPoolThread extends Thread implements PersistentByteBuffer{
	public static final int DEFAULT_INITIAL_BUFFER_SIZE = 1024*16;
	public static final int BUFFER_INCREMENT_FACTOR = 2;
	public static final int DEFAULT_MAX_BUFFER_SIZE = 1024*1024*32;

	private ByteBuffer buffer;
	
	
	public ByteBuffer getBuffer(){
		return buffer;
	}
	
	public ByteBuffer enlargeBuffer(){
		int size = buffer.capacity()*BUFFER_INCREMENT_FACTOR;
		int max = Integer.getInteger("histream.messageBufferMaxSize", DEFAULT_MAX_BUFFER_SIZE);
		if( size > max )size = max;
		if( size > buffer.capacity() ){
			// TODO: what happens if max exceeds size? Error?
			ByteBuffer newBuffer = ByteBuffer.allocate(size);
			newBuffer.put(buffer);
			newBuffer.flip();
			buffer = newBuffer;
			return buffer;
		}else{
			return null;
		}
	
	}
	
	public void initBuffer(){
		if( buffer == null ){
			buffer = ByteBuffer.allocate(Integer.getInteger("histream.messageBufferSize",DEFAULT_INITIAL_BUFFER_SIZE));
		}
		buffer.clear();
	}
	
	public MLLPoolThread(ThreadGroup group, Runnable r){
		super(group,r);
	}
}
