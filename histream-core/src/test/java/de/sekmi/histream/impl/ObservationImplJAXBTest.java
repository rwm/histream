package de.sekmi.histream.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;






import org.xml.sax.SAXException;

import de.sekmi.histream.DateTimeAccuracy;

public class ObservationImplJAXBTest {
	public static final File EXAMPLE_FACT_XSD = new File("examples/fact.xsd");
	public static final File[] EXAMPLE_FACT_FILES = new File[]{
		new File("examples/fact1.xml")
	};
	
	JAXBContext jaxb;
	ObservationFactoryImpl of;
	
	@Before
	public void initialize() throws JAXBException{
		jaxb = JAXBContext.newInstance(ObservationImpl.class);
		of = new ObservationFactoryImpl();
	}
	
	private static final int NUM_OBSERVATIONS = 4;
	// string observation
	private ObservationImpl createObservation(int index){
		ObservationImpl o = new ObservationImpl(of);
		o.conceptId = "C"+index;
		o.patientId = "P"+index;
		o.startTime = new DateTimeAccuracy(2015,1,1,index);
		switch( index ){
		case 0:
			// string value
			o.setValue(new StringValue("strval"));
			break;
		case 1:
			// numeric value without modifiers
			o.setValue(new NumericValue(BigDecimal.TEN));
			break;
		case 2:
			o.setValue(new NumericValue(BigDecimal.TEN));
		case 3:
			// no value with modifiers
			o.addModifier("M:1", new NumericValue(BigDecimal.ONE, "mm"));
			o.addModifier("M:2", new StringValue("lalala"));
			o.addModifier("M:3", null);
			break;
		default:
			throw new IllegalArgumentException("No test observation with number "+index);
		}
		return o;
	}
	@Test
	public void testMarshal() throws JAXBException{
		for( int i=0; i<NUM_OBSERVATIONS; i++ ){
			DOMResult dom = new DOMResult();
			ObservationImpl o = createObservation(i);
			JAXB.marshal(o, dom);
		}
	}
	@Test
	public void testMarshalUnmarshal() throws Exception{
		Marshaller m = jaxb.createMarshaller();
		Unmarshaller u = jaxb.createUnmarshaller();
		
		for( int i=0; i<NUM_OBSERVATIONS; i++ ){
			ObservationImpl o1 = createObservation(i);
			StringWriter s = new StringWriter();
			Object o2;
			try{
				m.marshal(o1, s);
				o2 = u.unmarshal(new StringReader(s.toString()));
			}catch( Throwable e ){
				throw new Exception("Error for observation "+i, e);
			}
			// verify class
			Assert.assertEquals(ObservationImpl.class, o2.getClass());
			// verify values
			Assert.assertEquals(o1.getValue(), ((ObservationImpl)o2).getValue());
		}
	}
	
	@Test
	public void parseExampleFacts() throws JAXBException{
		Unmarshaller u = jaxb.createUnmarshaller();

		for( int i=0; i<EXAMPLE_FACT_FILES.length; i++ ){
			Object obj = u.unmarshal(EXAMPLE_FACT_FILES[i]);
			Assert.assertEquals(ObservationImpl.class, obj.getClass());
			//ObservationImpl o = (ObservationImpl)obj;
		}
		
	}
	
	@Test
	public void validateExampleFacts() throws IOException, SAXException{
	    // create a SchemaFactory capable of understanding WXS schemas
	    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	    // load a WXS schema, represented by a Schema instance
	    Schema schema = factory.newSchema(EXAMPLE_FACT_XSD);

	    // create a Validator instance, which can be used to validate an instance document
	    Validator validator = schema.newValidator();

	    // validate the DOM tree
		for( int i=0; i<EXAMPLE_FACT_FILES.length; i++ ){
			validator.validate(new StreamSource(EXAMPLE_FACT_FILES[i]));
		}
	}
	
	public static void main(String args[]) throws JAXBException{
		ObservationImplJAXBTest oj = new ObservationImplJAXBTest();
		oj.initialize();
		Marshaller m = oj.jaxb.createMarshaller();
		m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		for( int i=0; i<NUM_OBSERVATIONS; i++ ){
			ObservationImpl o = oj.createObservation(i);
			m.marshal(o, System.out);
		}
		
	}
}
