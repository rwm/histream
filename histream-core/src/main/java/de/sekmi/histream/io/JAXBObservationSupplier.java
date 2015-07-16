package de.sekmi.histream.io;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;






import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationFactory;
import de.sekmi.histream.impl.ObservationImpl;

public class JAXBObservationSupplier extends XMLObservationSupplier {
	private Unmarshaller unmarshaller;
	
	public JAXBObservationSupplier(ObservationFactory factory, InputStream input)
			throws XMLStreamException, FactoryConfigurationError, JAXBException {
		super(factory, input);
		unmarshaller = JAXBContext.newInstance(ObservationImpl.class).createUnmarshaller();
		
	}

	@Override
	protected Observation readObservation()throws XMLStreamException{
		if( reader.isWhiteSpace() ){
			reader.nextTag();
		}
		// </facts> might occur after previous call to readObservation()
		while( reader.isEndElement() ){
			switch( reader.getLocalName() ){
			case "facts":
				// end of facts
				reader.nextTag();
			case "visit":
				// end of visit
				reader.nextTag();
				if( reader.isStartElement() && reader.getLocalName().equals("visit") ){
					// next visit
					readVisit();
				}
				break;
			case "dwh-eav":
				// end of document
				return null;
			}
		}
		// start element of eav-item or eav-group
		if( !reader.isStartElement() 
				|| !(reader.getLocalName().equals("fact")) ){
			throw new XMLStreamException("Element fact expected instead of "+reader.getLocalName(), reader.getLocation());
		}
		ObservationImpl fact;
		try {
			fact = (ObservationImpl)unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			throw new XMLStreamException( e);
		}
		if( fact.getPatientId() == null ){
			fact.setPatientId(visitData.get("patid"));
		}
		if( fact.getStartTime() == null ){
			fact.setStartTime(encounterStart);
		}
		// TODO set etc. from visit
		
		// TODO set ObservationFactory, initialize extensions
		return fact;
	}
}
