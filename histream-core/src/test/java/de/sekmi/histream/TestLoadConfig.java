package de.sekmi.histream;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;


import org.junit.Assert;
import org.junit.Test;

import de.sekmi.histream.conf.Configuration;

public class TestLoadConfig {

	@Test
	public void loadNewConfig(){
		Configuration config = Configuration.fromFile(new File("examples/histream.xml"));
		Assert.assertNotNull(config);
		Assert.assertEquals(5, config.getPlugins().length);
		Assert.assertEquals(1, config.getDestinations().length);
		
		Assert.assertEquals(config.getPlugins()[2], config.getDestinations()[0].getPlugin());
	}
}
