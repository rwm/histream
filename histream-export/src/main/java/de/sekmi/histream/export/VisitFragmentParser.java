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
	
	protected VisitFragmentParser() throws XMLStreamException, ParserConfigurationException {
		super();
		setFormatted(false);
		factory = XMLOutputFactory.newFactory();
		factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
		createDocument();
		// write meta data to document node
		setDOMWriter(doc);
	}
	
	private void setDOMWriter(Node node) throws XMLStreamException{
		Result result = new DOMResult(node);
		this.writer = factory.createXMLStreamWriter(result);
		
	}
	private void createDocument() throws ParserConfigurationException{
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = builder.newDocument();
		doc.getDomConfig().setParameter("namespaces", true);
		doc.getDomConfig().setParameter("namespace-declarations", true);
		//return doc;
	}


	@Override
	protected void endPatient(Patient patient) throws ObservationException {
		// TODO Auto-generated method stub
		super.endPatient(patient);
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
		// write visit info to visit fragment
		currentVisit = doc.createDocumentFragment();
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
		Node node = currentVisit.getFirstChild();
		Objects.requireNonNull(node);
		visitFragment(currentVisit.getFirstChild());
	}
	
	protected abstract void visitFragment(Node visit);
}
