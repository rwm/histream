/* Copyright (c) 2012, Raphael W. Majeed
 * All rights reserved. 
 * This file is subject to the conditions of the Simplified BSD License.
 * See LICENSE.txt or http://sourceforge.net/projects/histream/files/LICENSE.txt/view
 */
package histream.io;

import java.nio.ByteBuffer;

public interface AcknowledgeProvider {
	// TODO: ProcessedMessage might contain processing errors...
	ByteBuffer generateAcknowledge(ProcessedMessage message);
	ByteBuffer generateParserErrorResponse(MLLPacketException error);
	ByteBuffer generateParserErrorResponse(ByteBuffer data, Exception error);
}
