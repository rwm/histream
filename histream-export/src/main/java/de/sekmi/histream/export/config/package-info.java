/**
 * Export configuration with JAXB annotations
 * 
 */
@javax.xml.bind.annotation.XmlSchema(
		namespace="http://sekmi.de/ns/histream/export-v1",
		elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED, 
		xmlns = { 
				@javax.xml.bind.annotation.XmlNs(
						namespaceURI = "http://sekmi.de/ns/histream/export-v1", 
						prefix = "") 
				}
		)

package de.sekmi.histream.export.config;