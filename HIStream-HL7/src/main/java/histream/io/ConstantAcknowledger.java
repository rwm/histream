/* Copyright (c) 2012, Raphael W. Majeed
 * All rights reserved. 
 * This file is subject to the conditions of the Simplified BSD License.
 * See LICENSE.txt or http://sourceforge.net/projects/histream/files/LICENSE.txt/view
 */
package histream.io;

import java.nio.ByteBuffer;

/**
 * Acknowledger which accepts any MLL part with the byte 0x06
 * @author marap1
 *
 */
public class ConstantAcknowledger implements AcknowledgeProvider {
	private final ByteBuffer respondAcknowledge;
	private final ByteBuffer respondError;
	
	public static final ConstantAcknowledger ACCEPTANY_0x06 = new ConstantAcknowledger(new byte[]{0x06}, new byte[]{0x06});
	
	
	public ConstantAcknowledger(byte[] ackResponse, byte[] errorResponse){
		respondAcknowledge = ByteBuffer.wrap(ackResponse);
		respondError = ByteBuffer.wrap(errorResponse);
	}

	
	@Override
	public ByteBuffer generateAcknowledge(ProcessedMessage message) {
		return respondAcknowledge.asReadOnlyBuffer();
	}

	@Override
	public ByteBuffer generateParserErrorResponse(MLLPacketException error) {
		return respondError.asReadOnlyBuffer();
	}


	@Override
	public ByteBuffer generateParserErrorResponse(ByteBuffer data,
			Exception error) {
		return respondError.asReadOnlyBuffer();
	}

}
