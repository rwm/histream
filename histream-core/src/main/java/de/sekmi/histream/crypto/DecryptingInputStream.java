package de.sekmi.histream.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class DecryptingInputStream extends InputStream {
	private Cipher cipher;
	private InputStream in;
	private ByteBuffer buffer;
	private ByteBuffer outputBuffer;
	private boolean endOfStream;
	
	public DecryptingInputStream(InputStream in, Key asymmetricKey) throws GeneralSecurityException, IOException{
		this(in, "AES",128,"RSA", asymmetricKey);
	}
	public DecryptingInputStream(InputStream in, String symmetricAlgorithm, int symmetricKeysize, String asymmetricCipher, Key asymmetricKey) throws GeneralSecurityException, IOException{
		// use buffer
		endOfStream = false;
		byte[] buf = new byte[1024*8];
		int read = in.read(buf);
		buffer = ByteBuffer.wrap(buf, 0, read);
		int version = buffer.getInt();
		int ks = buffer.getShort();
		if( version != EncryptingByteChannel.Version )throw new IOException("Unsupported MDAT stream version "+version);
		byte[] wrapped = new byte[ks];
		buffer.get(wrapped);
		
		Cipher unwrap;
		try {
			unwrap = Cipher.getInstance(asymmetricCipher);
			unwrap.init(Cipher.UNWRAP_MODE, asymmetricKey);
			Key temp = unwrap.unwrap(wrapped, symmetricAlgorithm, Cipher.SECRET_KEY);
			cipher = Cipher.getInstance(symmetricAlgorithm);
			cipher.init(Cipher.DECRYPT_MODE, temp);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw new IOException("Unable to unwrap symmetric key",e);
		}
		// decrypt remaining buffer
		outputBuffer = ByteBuffer.allocate(1024*8*2);
		cipher.update(buffer, outputBuffer);
		// prepare buffer for writing
		buffer.compact();
		// prepare decrypted buffer for reading
		outputBuffer.flip();

		this.in = in;
	}


	@Override
	public void close() throws IOException {
		// XXX close underlying stream?
		in.close();
	}

	private int readAndDecrypt() throws IOException{
		int bytesRead=0;
		if( buffer.hasRemaining() ){
			// space available for reading data
			int pos = buffer.position();
			bytesRead = in.read(buffer.array(),buffer.arrayOffset()+pos,buffer.remaining());
			// got some bytes?
			if( bytesRead == -1 ){
				// no more data
				endOfStream = true;
			}else{
				// update position
				buffer.position(pos+bytesRead);
			}
		}
		outputBuffer.compact();
		buffer.flip();
		try {
			if( endOfStream == true ){
				if( buffer.hasRemaining() ){
					bytesRead = cipher.doFinal(buffer, outputBuffer);
				}else{
					byte[] fin = cipher.doFinal(); 
					outputBuffer.put(fin);
					bytesRead = fin.length;
				}
			}else{
				bytesRead = cipher.update(buffer, outputBuffer);
			}
		} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		} finally {
			buffer.compact();
			outputBuffer.flip();
		}
		return bytesRead;
	}
	@Override
	public int read() throws IOException {

		if( outputBuffer.hasRemaining() ){
			// remove from output buffer
			return outputBuffer.get();
		}else if( endOfStream )
			return -1; // nothing to do
		
		// if empty, fill output buffer
		readAndDecrypt();
		return read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int remaining = outputBuffer.remaining();
		if( remaining > 0 ){
			remaining = Math.min(remaining, len);
			outputBuffer.get(b, off, remaining);
			return remaining;
		}else if( endOfStream )
			return -1; // nothing to do

		// if empty, fill output buffer
		readAndDecrypt();
		return read(b, off, len);
	}
	@Override
	public int available() throws IOException {
		return outputBuffer.remaining();
	}
}
