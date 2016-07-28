package de.sekmi.histream.export;

import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import de.sekmi.histream.ObservationException;
import de.sekmi.histream.ext.Patient;
import de.sekmi.histream.ext.Visit;
import de.sekmi.histream.io.GroupedXMLWriter;

public abstract class VisitFragmentParser extends GroupedXMLWriter {
	private XMLOutputFactory factory;
	private Document doc;
	private DocumentFragment currentPatient;
	private DocumentFragment currentVisit;
	private boolean firstVisit;
	
	protected VisitFragmentParser() throws XMLStreamException, ParserConfigurationException {
		super();
		setFormatted(false);
		factory = XMLOutputFactory.newFactory();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		createDocument();
		// write meta data to document node
		setDOMWriter(doc);
	}
	
	private void fixNamespaces(DocumentFragment fragment){
		//fragment.setPrefix(null);
		// cannot do this
	}
	
	private void setDOMWriter(Node node) throws XMLStreamException{
		Result result = new DOMResult(node);
		writer = factory.createXMLStreamWriter(result);
		
		// XXX need this?
		//writer.setDefaultNamespace(ObservationImpl.XML_NAMESPACE);
		//writer.setPrefix(XMLConstants.DEFAULT_NS_PREFIX, ObservationImpl.XML_NAMESPACE);
		//writer.setPrefix("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		
	}
	private void createDocument() throws ParserConfigurationException{
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		f.setIgnoringComments(true);
		f.setCoalescing(true);
		f.setIgnoringElementContentWhitespace(true);
		f.setNamespaceAware(true);
		DocumentBuilder builder = f.newDocumentBuilder();
		doc = builder.newDocument();
		doc.getDomConfig().setParameter("namespaces", true);
		doc.getDomConfig().setParameter("namespace-declarations", true);
		//return doc;
	}


	@Override
	protected void endPatient(Patient patient) throws ObservationException {
		super.endPatient(patient);
		if( firstVisit == true ){
			// patient fragment already processed
			firstVisit = false;
		}else{
			// No visit for patient.
			// process patient fragment anyway
			patientFragment(currentPatient.getFirstChild());
		}
	}

	@Override
	protected void beginPatient(Patient patient) throws ObservationException {
		// write patient info to patient fragment
		currentPatient = doc.createDocumentFragment();

		try {
			setDOMWriter(currentPatient);
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
		super.beginPatient(patient);
	}

	@Override
	protected void beginEncounter(Visit visit) throws ObservationException {
		if( firstVisit == false ){
			// patient fragment was parsed
			fixNamespaces(currentPatient);
			patientFragment(currentPatient.getFirstChild());
			firstVisit = true;
		}

		// write visit info to visit fragment
		currentVisit = doc.createDocumentFragment();
		// XXX verify default namespace
		//currentVisit.isDefaultNamespace(namespaceURI)
		try {
			setDOMWriter(currentVisit);
		} catch (XMLStreamException e) {
			throw new ObservationException(e);
		}
		super.beginEncounter(visit);
	}

	@Override
	protected void endEncounter(Visit visit) throws ObservationException {
		super.endEncounter(visit);
		// encounter is finished
		// fragment should contain exactly one node -> the visit
		fixNamespaces(currentVisit);
		Node node = currentVisit.getFirstChild();
		Objects.requireNonNull(node);
		visitFragment(currentVisit.getFirstChild());
	}
	/**
	 * Called after each patient fragment was parsed.
	 * The patient fragment does not contain any encounters,
	 * these are provided to {@link #visitFragment(Node)}.
	 * @param patient patient node
	 * @throws ObservationException error
	 */
	protected void patientFragment(Node patient)throws ObservationException{
		
	}
	/**
	 * Called for each parsed visit fragment. The visit
	 * fragment will contain facts.
	 * 
	 * @param visit visit node
	 * @throws ObservationException error
	 */
	protected abstract void visitFragment(Node visit) throws ObservationException;
}
