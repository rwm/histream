/* Copyright (c) 2012, Raphael W. Majeed
 * All rights reserved. 
 * This file is subject to the conditions of the Simplified BSD License.
 * See LICENSE.txt or http://sourceforge.net/projects/histream/files/LICENSE.txt/view
 */
package histream.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class MinimallyProcessedMessage extends AbstractProcessedMessage {

	private String messageControlId;
	
	public MinimallyProcessedMessage(ByteBuffer rawBytes){
		ByteBuffer data = rawBytes.asReadOnlyBuffer();
		
		int pos = data.position();
		int eos = 0;
		for( int i=pos; i<data.limit(); i++ ){
			if( data.get(i) == '\r' ){
				eos = i;
				break;
			}
		}
		if( eos != 0 && (eos-pos)>4 ){
			data.limit(eos);
			Charset charset = Charset.forName("ASCII");
			CharBuffer cb = charset.decode(data);
			String[] fields = new String[19];
			int field = 1;

			int i=cb.position();
			final char fieldSep = cb.get(i+3);
			
			do{
				for( i=cb.position(); i<cb.limit(); i++ ){
					if( cb.get(i) == fieldSep ){
						fields[field] = CharBuffer.wrap(cb, 0, i-cb.position()).toString();
						if( fields[field].length() == 0 )fields[field] = null;
						cb.position(i+1);
						field ++;
						// stop parsing after maximum number of interesting fields
						if( field == fields.length )break;
					}
				}
			}while( cb.position() > i && field < fields.length );
			// last field (in case of no trailing separator)
			if( cb.position() < cb.limit() && field < fields.length ){
				fields[field] = CharBuffer.wrap(cb,0,cb.remaining()).toString();
				field ++;
			}
	
			if( field > 10 )
				messageControlId = fields[10];
		}
		
	}
	
	@Override
	public String getMessageID() {
		return messageControlId;
	}

}
