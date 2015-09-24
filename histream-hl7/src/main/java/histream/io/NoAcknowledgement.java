package histream.io;

import java.nio.ByteBuffer;

public class NoAcknowledgement implements AcknowledgeProvider {

	@Override
	public ByteBuffer generateAcknowledge(ProcessedMessage message) {return null;}

	@Override
	public ByteBuffer generateParserErrorResponse(MLLPacketException error) {return null;}

	@Override
	public ByteBuffer generateParserErrorResponse(ByteBuffer data,Exception error) {return null;}
}
