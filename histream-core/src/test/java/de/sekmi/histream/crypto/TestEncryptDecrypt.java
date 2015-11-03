package de.sekmi.histream.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.junit.Before;
import org.junit.Test;

import de.sekmi.histream.io.Streams;

import org.junit.Assert;

public class TestEncryptDecrypt {
	private KeyPair keyPair;
	
	@Before
	public void generateKeys() throws NoSuchAlgorithmException{
		KeyPairGenerator generatorRSA = KeyPairGenerator.getInstance("RSA");
		generatorRSA.initialize(2048, new SecureRandom());
		keyPair = generatorRSA.generateKeyPair();
	}
	
	public static void assertEqualFiles(Path expected, Path actual) throws IOException{
		FileChannel e = FileChannel.open(expected, StandardOpenOption.READ);
		FileChannel a = FileChannel.open(actual, StandardOpenOption.READ);
		int bufferSize = 1024*1024*10;
		ByteBuffer eb = ByteBuffer.allocateDirect(bufferSize);
		ByteBuffer ab = ByteBuffer.allocateDirect(bufferSize);
		long pos = 0;
		while( true ){
			int er = e.read(eb);
			int ar = a.read(ab);
			
			Assert.assertEquals(er, ar);

			if( er == -1 )break;
			
			eb.compact();
			ab.compact();

			for( int i=0; i<er; i++ ){
				Assert.assertEquals("Position "+(pos+eb.position()), eb.get(), ab.get()); 
			}
			
			pos += er;
			
			eb.flip();
			ab.flip();
		}
		e.close();
		a.close();
	}
	
	@Test
	public void testEncryptDecryptChannels() throws GeneralSecurityException, IOException{
		
		Path source = Paths.get("examples/dwh-jaxb.xml");
		Path temp = Files.createTempFile("encrypted", ".enc");
		Path dec = Files.createTempFile("decrypted", ".xml");

		// encrypt file
		FileChannel out = FileChannel.open(temp, StandardOpenOption.WRITE);
		WritableByteChannel enc = new EncryptingByteChannel(out, keyPair.getPublic());
		
		FileChannel in = FileChannel.open(source);
		in.transferTo(0, Long.MAX_VALUE, enc);
		in.close();
		
		enc.close();
		out.close();
		
		// decrypt file
		in = FileChannel.open(temp, StandardOpenOption.READ);
		ReadableByteChannel decrypted = new DecryptingByteChannel(in, keyPair.getPrivate());
		out = FileChannel.open(dec, StandardOpenOption.WRITE);
		out.transferFrom(decrypted, 0, Long.MAX_VALUE);
		in.close();
		out.close();
		decrypted.close();
		
		Files.delete(temp);
		// compare files
		assertEqualFiles(dec, source);
		Files.delete(dec);
	}

	@Test
	public void testEncryptDecryptStreams() throws GeneralSecurityException, IOException{
		
		Path source = Paths.get("examples/dwh-jaxb.xml");
		Path temp = Files.createTempFile("encrypted", ".enc");
		Path dec = Files.createTempFile("decrypted", ".xml");

		// encrypt file
		OutputStream out = Files.newOutputStream(temp, StandardOpenOption.WRITE);
		OutputStream enc = new EncryptingOutputStream(out, keyPair.getPublic());
		
		InputStream in = Files.newInputStream(source);
		Streams.streamCopy(in, enc);
		in.close();
		
		enc.close();
		out.close();
		
		// decrypt file
		in = Files.newInputStream(temp, StandardOpenOption.READ);
		InputStream decrypted = new DecryptingInputStream(in, keyPair.getPrivate());
		out = Files.newOutputStream(dec, StandardOpenOption.WRITE);
		Streams.streamCopy(decrypted, out);
		in.close();
		out.close();
		decrypted.close();
		
		Files.delete(temp);
		// compare files
		assertEqualFiles(dec, source);
		Files.delete(dec);
	}

}
