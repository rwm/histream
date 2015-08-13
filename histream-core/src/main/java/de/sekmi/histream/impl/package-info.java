/**
 * Core implementation
 * 
 * @author marap1
 *
 */

@XmlSchema(namespace=ObservationImpl.XML_NAMESPACE,
elementFormDefault=XmlNsForm.QUALIFIED,
	xmlns = {
			
		@XmlNs(prefix = "xsi", namespaceURI = javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI) 
		//"http://www.w3.org/2001/XMLSchema-instance"
	}
)
package de.sekmi.histream.impl;

import javax.xml.bind.annotation.*;