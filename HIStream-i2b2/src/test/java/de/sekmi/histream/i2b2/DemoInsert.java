package de.sekmi.histream.i2b2;

import java.io.File;

import de.sekmi.histream.impl.RunConfiguration;

/**
 * Demo class to insert data from Eclipse IDE
 * 
 * @author Raphael
 *
 */
public class DemoInsert {

	public static void main(String[] args) throws Exception {
		String[] files = new String[1];
		files[0] = "c:/temp/dzl/eurIPF.txt";
		files[0] = "../histream-core/src/main/examples/dwh-flat.txt";
		RunConfiguration.readFiles(new File("c:/temp/dzl/histream.xml"),files);
	}

}
