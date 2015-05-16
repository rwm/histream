package histream.hl7.listener;

import histream.io.PersistentByteBuffer;

import java.nio.ByteBuffer;

public class PoolThread extends Thread implements PersistentByteBuffer{
	private int bufferInit;
	private int bufferMax;
	private float bufferIncrementFactor;
	private ByteBuffer buffer;
	
	
	public ByteBuffer getBuffer(){
		return buffer;
	}
	
	public ByteBuffer enlargeBuffer(){
		int size = (int)(buffer.capacity()*bufferIncrementFactor);
		if( size > bufferMax )size = bufferMax;
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
			buffer = ByteBuffer.allocate(bufferInit);
		}
		buffer.clear();
	}
	
	public PoolThread(ThreadGroup group, Runnable r){
		this(group,r,1024*16, 2f, 1024*1024*32);

	}
	public PoolThread(ThreadGroup group, Runnable r, int initialBufferSize, float incrementFactor, int maxSize){
		super(group,r);
		if( incrementFactor <= 1f )throw new IllegalArgumentException("Invalid buffer increment factor '"+incrementFactor+"'. Factor needs to be > 1!");
		this.bufferInit = initialBufferSize;
		this.bufferIncrementFactor = incrementFactor;
		this.bufferMax = maxSize;
	}
}