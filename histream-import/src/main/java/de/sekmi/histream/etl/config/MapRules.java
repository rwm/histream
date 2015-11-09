package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;

/**
 * Map rules for columns or concepts.
 * The actual mapping implementation is in
 * {@link Column#valueOf(Object, de.sekmi.histream.etl.MapFeedback)}
 * 
 * @author R.W.Majeed
 *
 */
public class MapRules extends MapReplace{

	@XmlElement(name="case")
	MapCase[] cases;
	@XmlElement(required=false)
	MapCase otherwise;
}
