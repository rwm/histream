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

public class DecryptingInputStream implements ReadableByteChannel {
	private Cipher cipher;
	private ReadableByteChannel in;
	private ByteBuffer buffer;
	private boolean endOfStream;
	
	public DecryptingInputStream(ReadableByteChannel in, Key asymmetricKey) throws GeneralSecurityException, IOException{
		this(in, "AES",128,"RSA", asymmetricKey);
	}
	public DecryptingInputStream(ReadableByteChannel in, String symmetricAlgorithm, int symmetricKeysize, String asymmetricCipher, Key asymmetricKey) throws GeneralSecurityException, IOException{
		// use buffer
		endOfStream = false;
		buffer = ByteBuffer.allocate(1024*10);

		in.read(buffer);
		buffer.flip();
		
		int version = buffer.getInt();
		int ks = buffer.getShort();
		if( version != EncryptingOutputStream.Version )throw new RuntimeException("Unsupported MDAT stream version "+version);
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
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		if( endOfStream )
			return 0; // nothing to do
		
		int bytesRead=0;
		if( buffer.hasRemaining() ){
			bytesRead = in.read(buffer);
		}
		buffer.flip();
		try {
			if( bytesRead == -1 ){
				endOfStream = true;
				return cipher.doFinal(buffer, dst);
			}else{
				return cipher.update(buffer, dst);
			}
		} catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		} finally {
			buffer.compact();			
		}
	}
}
