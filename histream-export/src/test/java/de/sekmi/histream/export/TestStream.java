package de.sekmi.histream.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

public class TestStream extends OutputStream{
	boolean isClosed;

	@Override
	public void write(int arg0) throws IOException {
		// do nothing
	}

	@Override
	public void close(){
		isClosed = true;
	}

	/**
	 * Verify that the close operation from {@link PrintWriter} will propagate through
	 * {@link OutputStreamWriter} to the underlying output stream.
	 */
	@Test
	public void verifyCascadedClose(){
		// no checked exceptions below this point (otherwise must make sure os is closed)
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(this, Charset.defaultCharset()));

		pw.println();
		pw.print("Only for test, will not be written");
		pw.flush();
		pw.close();
		Assert.assertTrue(isClosed);
	}
	
}
