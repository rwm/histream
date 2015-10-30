package de.sekmi.histream.crypto;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Wraps a WritableByteChannel with encryption. 
 * Closing the EncryptionOutputStream will close the underlying WritableByteChannel

 * @author Raphael
 *
 */
public class EncryptingOutputStream extends OutputStream{
	public static final int Version = 1;
	private Cipher cipher;
	private OutputStream out;
	
	public EncryptingOutputStream(OutputStream out, Key asymmetricKey) throws GeneralSecurityException, IOException{
		this(out, "AES",128,"RSA", asymmetricKey);
	}
	public EncryptingOutputStream(OutputStream out, String symmetricAlgorithm, int symmetricKeysize, String asymmetricCipher, Key asymmetricKey) throws GeneralSecurityException, IOException{
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
		ByteBuffer buffer = ByteBuffer.allocate(1024*10);
		
		// prefix output with version and key length
		buffer.putInt(Version).flip();
		out.write(buffer.array(),buffer.position(),buffer.remaining());
		buffer.clear();
		
		// write wrapped length
		buffer.putShort((short)wrapped.length).flip();
		out.write(buffer.array(),buffer.position(),buffer.remaining());
		buffer.clear();

		out.write(wrapped);
		
		// initialize symmetric cipher
		cipher = Cipher.getInstance(symmetricAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, sk);
	}
	@Override
	public void close() throws IOException {
		try {
			out.write(cipher.doFinal());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new IOException(e);
		}
		out.close();
	}
	@Override
	public void write(byte[] src, int off, int len) throws IOException {
		byte[] enc = cipher.update(src, off, len);
		out.write(enc);
	}
	@Override
	public void write(int b) throws IOException {
		write(new byte[]{(byte)b});
	}
	@Override
	public void write(byte[] b) throws IOException {
		byte[] enc = cipher.update(b);
		out.write(enc);
	}
}
