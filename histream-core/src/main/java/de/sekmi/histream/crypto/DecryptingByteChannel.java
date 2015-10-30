package de.sekmi.histream.crypto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public class DecryptingByteChannel implements ReadableByteChannel {
	private Cipher cipher;
	private ReadableByteChannel in;
	private ByteBuffer buffer;
	private boolean endOfStream;
	
	public DecryptingByteChannel(ReadableByteChannel in, Key asymmetricKey) throws GeneralSecurityException, IOException{
		this(in, "AES",128,"RSA", asymmetricKey);
	}
	public DecryptingByteChannel(ReadableByteChannel in, String symmetricAlgorithm, int symmetricKeysize, String asymmetricCipher, Key asymmetricKey) throws GeneralSecurityException, IOException{
		// use buffer
		endOfStream = false;
		buffer = ByteBuffer.allocate(1024*8);

		in.read(buffer);
		buffer.flip();
		
		int version = buffer.getInt();
		int ks = buffer.getShort();
		if( version != EncryptingByteChannel.Version )throw new IOException("Unsupported MDAT stream version "+version);
		byte[] wrapped = new byte[ks];
		buffer.get(wrapped);
		buffer.compact();
		
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

		this.in = in;
	}

	@Override
	public boolean isOpen() {
		return in.isOpen();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		if( endOfStream )
			return -1; // nothing to do
		
		int bytesRead=0;
		if( buffer.hasRemaining() ){
			bytesRead = in.read(buffer);
		}
		buffer.flip();
		try {
			if( bytesRead == -1 ){
				endOfStream = true;
				bytesRead = cipher.doFinal(buffer, dst);
				return bytesRead;
			}else{
				bytesRead = cipher.update(buffer, dst);
				return bytesRead;
			}
		} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		} finally {
			buffer.compact();			
		}
	}
}
