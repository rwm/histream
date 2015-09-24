package histream.io;

import java.nio.ByteBuffer;

public class MLLPacketException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ByteBuffer data;
	
	
	public MLLPacketException(ByteBuffer data, Throwable cause){
		super(cause);
		this.data = data;
	}
	
	public ByteBuffer getRawData(){
		return data.duplicate();
	}
}
