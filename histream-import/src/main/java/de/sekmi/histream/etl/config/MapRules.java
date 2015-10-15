package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * Map rules for columns or concepts
 * @author R.W.Majeed
 *
 */
public class MapRules {

	@XmlElement(name="case")
	MapCase[] cases;
	@XmlElement(required=false)
	MapCase otherwise;
}
