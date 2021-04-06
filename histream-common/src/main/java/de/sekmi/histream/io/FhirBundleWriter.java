package de.sekmi.histream.io;

import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;

import de.sekmi.histream.DateTimeAccuracy;
import de.sekmi.histream.Value;
import de.sekmi.histream.Value.Operator;
import de.sekmi.histream.impl.NumericValue;
import de.sekmi.histream.impl.StringValue;

/**
 * Write FHIR Bundle collections containing resources
 * of type Patient, Encounter and Observation.
 *
 * @author R.W.Majeed
 *
 */
public class FhirBundleWriter extends AbstractXMLStreamWriter {
	public static final String NAMESPACE = "http://hl7.org/fhir";
	private String fhirBaseUrl;

	public FhirBundleWriter(OutputStream output, String charsetEncoding) throws XMLStreamException {
		super(output, charsetEncoding);
	}

	public FhirBundleWriter(Result result) throws XMLStreamException {
		super(result);
	}

	public void writeStartDocument(HashMap<String, String> metaTags, Instant lastModified, Instant timestamp) throws XMLStreamException {
		this.fhirBaseUrl = "urn:local:/"; // TODO allow base url from metaTags

		writer.writeStartDocument();
		writer.setDefaultNamespace(NAMESPACE);
		// NamespaceContext is not supported by DOM stream writer
		writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX, "Bundle", NAMESPACE);
//		writer.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
// not needed as the default namespace is written via writeStartElement
//		writer.writeDefaultNamespace(NAMESPACE);
//		writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		formatPush();
		formatNewline();

		// write meta
		writeStartElement("meta");
		// doesn't work for DOM writers
		//Objects.requireNonNull(writer.getPrefix(NAMESPACE));
		writeStartElement("tag");
		writeStringValue("system", fhirBaseUrl+"System/TODO");
		writeStringValue("code", "TODO");
		writeEndElement();
		writeEndElement();

		
		writeStringValue("type", "collection");
/*
 *   <link>
      <relation value="fhir-base"/>
      <url value="http://example.com/base"/>
   </link>

 */
		writeStartElement("link");
		writeStringValue("relation", "fhir-base");
		writeStringValue("url", fhirBaseUrl);
		writeEndElement();
	}
	/**
	 * Write a FHIR timestamp element tag.
	 * @param element XML element name
	 * @param timestamp timestamp with accuracy
	 * @param zoneId time zone to localize the timestamp
	 * @throws XMLStreamException error
	 */
	public void writeTimestamp(String element, DateTimeAccuracy timestamp, ZoneId zoneId) throws XMLStreamException {
		writeStringValue(element, timestamp.toFhirDateTime(zoneId));
	}

	/**
	 * Write a value element typed as specified by the FHIR standard: valueQuantity, valueCodableConcept, valueString, valueBoolean, etc.
	 * @param value value
	 * @throws XMLStreamException error
	 */
	public void writeValueTyped(Value value) throws XMLStreamException{
		if( value instanceof NumericValue ) {
			writeValueQuantity((NumericValue)value);
		}else if( value instanceof StringValue ) {
			writeStringValue("valueString", value.getStringValue());
		}else {
			
		}
	}
	public void writeValueQuantity(NumericValue num) throws XMLStreamException {
		writeStartElement("valueQuantity");
		// actual value
		writeStringValue("value", num.getStringValue());
		// comparator
		Operator op = num.getOperator();
		if( op != null && op != Operator.Equal ) {
			String comp = null;
			switch( num.getOperator() ) {
			case GreaterOrEqual:
				comp = ">=";
				break;
			case GreaterThan:
				comp = ">";
				break;
			case LessOrEqual:
				comp = "<=";
				break;
			case LessThan:
				comp = "<";
				break;
			case NotEqual:
			case Equal:
			case Interval:
			default:
				// not supported
				// TODO raise warning
				break;
			}
			if( comp != null ) {
				writeStringValue("comparator", comp);
			}
		}
		// unit
		if( num.getUnits() != null ) {
			writeStringValue("unit", num.getUnits());
		}
		
		writeEndElement();

	}
	public void writeStringValue(String element, String value) throws XMLStreamException {
		formatIndent();
		writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,element,NAMESPACE);
		writer.writeAttribute("value", value);
		writer.writeEndElement();
		formatNewline();
	}

	public void writeStartElement(String name) throws XMLStreamException {
		formatIndent();
		// TODO write elements with namespaces -> ObservationImpl.XML_NAMESPACE, 
		writer.writeStartElement(XMLConstants.DEFAULT_NS_PREFIX,name,NAMESPACE);
		writer.setDefaultNamespace(NAMESPACE);
//		writer.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		formatNewline();
		formatPush();
	}

	public void writeEndElement() throws XMLStreamException {
//			formatIndent();
//			writer.writeEndElement(); // fact wrapper
//			formatNewline();
			formatPop();
			formatIndent();
			writer.writeEndElement(); // encounter
			formatNewline();
	}
	
	public void beginBundleEntry(String relativeUrl) throws XMLStreamException {
		writeStartElement("entry");
		if( relativeUrl != null ) {
			writeStringValue("fullUrl", fhirBaseUrl+relativeUrl);
		}
		writeStartElement("resource");
		
	}
	public void endBundleEntry() throws XMLStreamException {
		writeEndElement();
		writeEndElement();
	}
}
