package histream.hl7.filter;


import histream.hl7.Decoder;
import histream.hl7.Message;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * HL7v2 message decoder which enables filters to modify the message during the decoding process.
 * 
 * @author RWM
 *
 */
public class FilteringDecoder implements Decoder{
	private static Logger log = Logger.getLogger(FilteringDecoder.class.getName());
	private CharBuffer charBuffer;
	private ByteBuffer byteBuffer;
	private String defaultCharset;
	private boolean ignoreMessageCharset;
	private CharsetDecoder asciiDecoder;
	private Hashtable<String,CharsetDecoder> decoderCache;
	private List<MessageFilter> acceptedFilters;
	
	protected static final byte segmentTerminatorByte = 0x0D;
	protected static final char segmentTerminatorChar = '\r';
	
	
	ArrayList<String[]> message;
	protected ArrayList<MessageFilter> filters;
	
	public FilteringDecoder(String defaultCharset, boolean ignoreMessageCharset){
		this.defaultCharset = defaultCharset;
		this.ignoreMessageCharset = ignoreMessageCharset;

		acceptedFilters = new LinkedList<MessageFilter>();
		charBuffer = CharBuffer.allocate(1024*1024);		
		byteBuffer = ByteBuffer.allocate(1024*1024);
		
		filters = new ArrayList<MessageFilter>();
		decoderCache = new Hashtable<String, CharsetDecoder>();
		decoderCache.put(defaultCharset, Charset.forName(defaultCharset).newDecoder());
		asciiDecoder = Charset.forName("ASCII").newDecoder();
		asciiDecoder.onMalformedInput(CodingErrorAction.REPLACE);
		asciiDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}
	
	/**
	 * Get the default charset. The default charset is used to decode messages which do not
	 * specify any charset.
	 * @return charset name
	 */
	public String getDefaultCharset(){
		return defaultCharset;
	}
	
	/**
	 * Determine whether the message charset is ignored. If it is ignored, the
	 * default charset is used to decode all messages, regardles of the charset
	 * specified in the message.
	 * @return whether the message charset is ignored
	 */
	public boolean isMessageCharsetIgnored(){
		return ignoreMessageCharset;
	}
	
	public FilteringDecoder(){
		this(  "ISO-8859-1", false);
	}
	public void addFilter(MessageFilter filter){
		synchronized( filters ){
			filters.add(filter);
		}
	}
	public void removeFilter(MessageFilter filter){
		synchronized( filters ){
			filters.remove(filter);
		}
	}
	
	protected void filterMessage(Message message){
		/* filter message */
		for( MessageFilter f : acceptedFilters ){
			f.filter(message);
			// stop if message dropped
			if( message.numSegments() == 0 )break;
		}
	}
	
	protected void parseMessage(List<String[]> message, ByteBuffer src) {
		CharBuffer cb = charBuffer;
		ByteBuffer bb = byteBuffer;
		
		bb.clear();
		cb.clear();
		
		
		String[] fields = new String[100];
		
		/* parse first segment (should be msh) */
		char[] seg = new char[3];
		char fs; // field separator
		
		// decode bytes until first 0x0D
		while( src.hasRemaining() ){
			byte b = src.get();
			if( b == segmentTerminatorByte )break;
			bb.put(b);
		}
		bb.flip();

		asciiDecoder.reset();
		asciiDecoder.decode(bb, cb, true);
		asciiDecoder.flush(cb);
		
		cb.flip();
		
		// read segment name and field separator
		cb.get(seg);
		fs = cb.get();
		// TODO: exception/error
		assert seg[0]=='M' && seg[1]=='S' && seg[2]=='H' : "MSH segment expected, but found "+new String(seg);

		fields[0] = "MSH";
		fields[1] = new String(new char[]{fs});

		/* read to end of MSH segment */
		int f = 2;
		StringBuilder sb = new StringBuilder(1024);
		
		while( cb.hasRemaining() ){
			char c = cb.get();
			if( c == fs ){
				/* next field */
				fields[f] = sb.toString();
				f ++;
				sb.delete(0, sb.length());
			}else sb.append(c);
		}
		if( sb.length() != 0 ){
			// last segment after fs
			fields[f] = sb.toString();
			f ++;
		}
				
		/* copy string array */
		String[] msh = Arrays.copyOf(fields, f);
		
		/* initialize filters */
		this.acceptedFilters.clear();
		synchronized( filters ){
			for( int i=0; i<filters.size(); i++ ){
				MessageFilter filter = filters.get(i);
				/* filter providers might choose not to filter
				 * a message by returning null.
				 */
				int policy = filter.previewMSH(msh);
				if( policy == 0 )continue;
				else if( policy == 1 ){			
					// filter message
					acceptedFilters.add(filter);
				}else if( policy == -1 ){
					// drop whole message
					message.clear();
					return;
				}else{
					log.warning("Ignoring unsupported return value "+policy+" by filter "+filter+" (.previewMSH)");
				}
			}
		}
		// add MSH segment
		message.add(msh);
		
		// load charset provider
		String charset = null;
		if( ignoreMessageCharset == false && msh.length > 18 && msh[18].length() > 0 )charset = msh[18];
		else charset = defaultCharset;
		
		CharsetDecoder decoder = decoderCache.get(charset);
		if( decoder == null ){
			try{
				decoder = Charset.forName(charset).newDecoder();
				decoder.onMalformedInput(CodingErrorAction.REPLACE);
				decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
				// store encoder for reuse
				decoderCache.put(charset, decoder);
			}catch( IllegalCharsetNameException e ){
				log.log(Level.WARNING,"Illegal charset name '"+msh[18]+"' in MSH segment. Using default charset.", e);
			}catch( UnsupportedCharsetException e ){
				log.log(Level.WARNING,"Unsupported charset name '"+msh[18]+"' in MSH segment. Using default charset.", e);
			}
		}
		if( decoder == null )decoder = Charset.forName(defaultCharset).newDecoder();
		decoder.reset();
		cb.clear();
		decoder.decode(src, cb, true);
		decoder.flush(cb);
		cb.flip();
		
		/* HL7 documentation says: for multibyte
		 * charsets separator chars are still single byte.
		 * TODO: split bytes by separator  
		 */
		
		/* XXX fast copy of remaining message,
		 * if no filter necessary.
		 */
		if( acceptedFilters.size() == 0 && src.hasRemaining() ){
		}
		
		/* filter remaining segments
		 */
		while( cb.hasRemaining() ){

			/* load next segment */
			f = 0;
			sb.delete(0, sb.length());
			while( cb.hasRemaining() ){
				char c = cb.get();
				
				if( c == fs || c == segmentTerminatorChar ){
					/* next field */
					fields[f] = sb.toString();
					f ++;
					sb.delete(0, sb.length());
					
					/* end of segment */
					if( c == segmentTerminatorChar )break;
				}else sb.append(c);
			}
			/* add segment */
			message.add( Arrays.copyOf(fields, f) );
		}

	}
	@Override
	public Message decode(ByteBuffer in) {
		List<String[]> segments = new ArrayList<String[]>();
		//int length = in.remaining();
		parseMessage(segments, in);
		Message m = new Message(segments);
		if( m.numSegments() > 0 )filterMessage(m);
		
		/* also possible to m.compact() message. This would 
		 * save some space but has negative impact on performance
		 * (since new arrays are allocated)
		 * Maybe benchmark before deciding.
		 */
		return m;
	}
	
	/*
	public static final Message _decode(ByteBuffer in){
		return new FilteringDecoder(OsgiTools.systemProperties("histream.hl7.")).decode(in);
	}
	*/
	public static final Message _decode(ByteBuffer in){
		return new FilteringDecoder().decode(in);
	}
}
