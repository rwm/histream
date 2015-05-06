package de.sekmi.histream;

import java.io.File;

import javax.xml.bind.JAXB;

import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.conf.Configuration;

public class TestLoadConfig {

	@Test
	public void loadNewConfig(){
		Configuration config = JAXB.unmarshal(new File("src/test/resources/histream.xml"), Configuration.class);
		Assert.assertNotNull(config);
	}
}
