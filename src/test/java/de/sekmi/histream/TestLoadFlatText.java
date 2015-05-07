package de.sekmi.histream;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Test;

public class TestLoadFlatText {

	@Test
	public void loadFlatText() throws Exception{
		BufferedReader buf = new BufferedReader(new FileReader("src/test/resources/fwh-flat.txt"));
		//buf.lines().forEach(action);
		buf.lines();
		buf.close();
	}
}
