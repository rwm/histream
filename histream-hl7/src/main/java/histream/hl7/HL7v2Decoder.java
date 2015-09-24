package histream.hl7;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import histream.io.MLLFileMessageHandler;
import histream.io.MLLPacketHandler;
import histream.io.ProcessedMessage;

public class HL7v2Decoder implements MLLPacketHandler, Closeable{

	private Decoder decoder;
	private MessageHandler handler;
	private MLLFileMessageHandler dumpfile;
	private Object dumplock;
	
	public HL7v2Decoder(Decoder decoder, MessageHandler handler){
		this.decoder = decoder;
		this.handler = handler;
		this.dumpfile = null;
		this.dumplock = new Object();
	}
	
	/**
	 * Specify a file where all messages are dumped to.
	 * @param file Dump file
	 * @param gzip whether to apply gzip compression.
	 */
	public void setDumpfile(File file, boolean gzip)throws IOException{
		synchronized( dumplock ){
			if( dumpfile != null ){
				dumpfile.close();
			}
		
			if( file == null ){
				dumpfile = null;
			}else{
				dumpfile = gzip?MLLFileMessageHandler.GZippedHandler(file):new MLLFileMessageHandler(file);
				//dumpfile.open();
			}
		}
	}
	
	@Override
	public ProcessedMessage processMessage(ByteBuffer message){
		if( dumpfile != null ){
			synchronized( dumplock ){
				try{
					dumpfile.writeMessage(message);
				}catch( IOException e ){
					// TODO: write to log
					// stop writing after IOException
					dumpfile = null;
				}
			}
		}
		Message m;
		synchronized( decoder ){
			// TODO: allow parallelization: e.g. use resource manager w/ multiple decoders

			m = decoder.decode(message);
		}
		// TODO: how to handle dropped messages? maybe set a flag 'dropped', instead of deleting all segments
		return handler.processMessage(m);
	}
	
	public void open(){
	}

	@Override
	public void close() {
		if( dumpfile != null ){
			dumpfile.close();
		}
	}

}
