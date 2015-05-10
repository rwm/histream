package de.sekmi.histream;

import java.io.File;


import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.conf.Configuration;

public class TestLoadConfig {

	@Test
	public void loadNewConfig(){
		Configuration config = Configuration.fromFile(new File("src/test/resources/histream.xml"));
		Assert.assertNotNull(config);
		Assert.assertEquals(6, config.getPlugins().length);
		Assert.assertEquals(1, config.getDestinations().length);
		
		Assert.assertEquals(config.getPlugins()[2], config.getDestinations()[0].getPlugin());
	}
}
