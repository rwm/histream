package histream.io;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * AcknowledgeProvider which sends simple HL7 acknowledge messages
 * 
 * @author marap1
 *
 */
public class SimpleHL7Acknowledger implements AcknowledgeProvider {
	final Charset encoding;
	final SimpleDateFormat hl7date;
	final ByteBuffer emptyBuffer;
	final AtomicInteger responseCounter;
	
	static final Logger log = Logger.getLogger(SimpleHL7Acknowledger.class.getName());
	
	public SimpleHL7Acknowledger(){
		encoding = Charset.forName("ASCII");
		hl7date = new SimpleDateFormat("yyyyMMddHHmmss");		
		emptyBuffer = ByteBuffer.wrap(new byte[]{});
		responseCounter = new AtomicInteger(0);
	}
	
	@Override
	public ByteBuffer generateAcknowledge(ProcessedMessage message) {
		StringBuilder ack = getMSH4Ack(message.getMessageID(),"AA","OK");
		if( ack != null )return encoding.encode(ack.toString());
		else return emptyBuffer; // TODO: send default error
	}

	@Override
	public ByteBuffer generateParserErrorResponse(ByteBuffer data,	Exception error) {
		MinimallyProcessedMessage min = new MinimallyProcessedMessage(data);
		StringBuilder resp = getMSH4Ack(min.getMessageID(),"AE",error.toString());
		if( resp != null )return encoding.encode(resp.toString());
		else return emptyBuffer; // TODO: send error
	}

	@Override
	public ByteBuffer generateParserErrorResponse(MLLPacketException error) {
		return generateParserErrorResponse(error.getRawData(),error);
	}
	
	private StringBuilder getMSH4Ack(String messageId, String ackCode, String ackMessage){
		StringBuilder ack = new StringBuilder();
		ack.append("MSH|^~\\&|");
		// use receiving app/fac as sender
		// 3: sending app
		ack.append("histream").append('|');
		// 4: sending facility
		ack.append("").append('|');
		
		// use receiving app/fac as sender
		// 5: receiving app
		ack.append("").append('|');
		// 6: receiving facility
		ack.append("").append('|');
		
		// 7: date time of message
		ack.append(hl7date.format(new Date())).append('|');
		// 8: security
		ack.append('|');
		// 9: message type (TODO: maybe add event)
		ack.append("ACK").append('|');
		// 10: message control id
		ack.append(responseCounter.incrementAndGet()).append('|');
		// 11: processing id
		ack.append("P").append('|');
		// 12: processing id
		ack.append("2.4");
		// end of segment
		ack.append('\r'); // standard conform only \r
		
		// ACK segment
		ack.append("MSA|");
		ack.append(ackCode).append('|');
		// 2: message control id
		ack.append(messageId).append('|');
		// 3: text message
		ack.append(ackMessage);
		ack.append('\r');
		return ack;
	}

}
