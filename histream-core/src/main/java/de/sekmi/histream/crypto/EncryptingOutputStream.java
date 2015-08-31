package de.sekmi.histream.crypto;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

public class EncryptingOutputStream implements WritableByteChannel{
	public static final int Version = 1;
	private Cipher cipher;
	private WritableByteChannel out;
	private ByteBuffer buffer;
	
	public EncryptingOutputStream(WritableByteChannel out, Key asymmetricKey) throws GeneralSecurityException, IOException{
		this(out, "AES",128,"RSA", asymmetricKey);
	}
	public EncryptingOutputStream(WritableByteChannel out, String symmetricAlgorithm, int symmetricKeysize, String asymmetricCipher, Key asymmetricKey) throws GeneralSecurityException, IOException{
		KeyGenerator kg = KeyGenerator.getInstance(symmetricAlgorithm);
		//log.fine("Generating symmetric key "+symmetricAlgorithm+" with size "+symmetricKeysize);
		kg.init( symmetricKeysize );
		SecretKey sk = kg.generateKey();
		// wrap with asymmetric algorithm and write to output
		Cipher wrapper = Cipher.getInstance(asymmetricCipher);
		wrapper.init(Cipher.WRAP_MODE, asymmetricKey);
		byte[] wrapped = wrapper.wrap(sk);
		this.out = out;
		
		// use buffer
		buffer = ByteBuffer.allocate(1024*10);
		
		// prefix output with version and key length
		buffer.putInt(Version).flip();
		out.write(buffer);
		buffer.clear();
		
		// write wrapped length
		buffer.putShort((short)wrapped.length).flip();
		out.write(buffer);
		buffer.clear();

		out.write(ByteBuffer.wrap(wrapped));
		
		// initialize symmetric cipher
		cipher = Cipher.getInstance(symmetricAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, sk);
	}
	@Override
	public boolean isOpen() {
		return out.isOpen();
	}
	@Override
	public void close() throws IOException {
		if( buffer.hasRemaining() ){
			out.write(buffer);
		}
		try {
			out.write(ByteBuffer.wrap(cipher.doFinal()));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}
		out.close();
	}
	@Override
	public int write(ByteBuffer src) throws IOException {
		try {
			cipher.update(src, buffer);
		} catch (ShortBufferException e) {
			throw new IOException(e);
		} finally {
			buffer.flip();
		}
		return out.write(buffer);
	}
}
