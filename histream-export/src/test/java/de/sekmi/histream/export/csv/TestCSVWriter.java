package de.sekmi.histream.export.csv;

import java.nio.file.Paths;

import static org.junit.Assert.*;
import org.junit.Test;

public class TestCSVWriter {

	@Test
	public void verifyEscape(){
		CSVWriter w = new CSVWriter(Paths.get("."), '\t', ".txt");
		// null is written as empty string (default)
		assertEquals("", w.escapeData(null));
		// line feeds and carriage returns will be encoded each as single spaces
		assertEquals("a  b", w.escapeData("a\r\nb"));
	}
}
