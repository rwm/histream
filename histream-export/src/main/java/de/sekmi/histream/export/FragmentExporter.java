package de.sekmi.histream.export;

import java.io.IOException;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import de.sekmi.histream.ObservationException;
import de.sekmi.histream.export.config.Concept;
import de.sekmi.histream.export.config.ConceptGroup;
import de.sekmi.histream.export.config.EavTable;
import de.sekmi.histream.export.config.ExportDescriptor;
import de.sekmi.histream.export.config.ExportException;

/**
 * Export visit fragments.
 * <p>
 * All {@link ObservationException}s generated by this class
 * are caused by either {@link ExportException} or {@link IOException}.
 * </p>
 *
 * @author R.W.Majeed
 *
 */
class FragmentExporter extends VisitFragmentParser {

	private TableParser patientParser;
	private TableParser visitParser;
	private EavTableParser[] eavParsers;

	private Element currentPatient;
	private FactClassAnnotator factAnnotator;
	
	private void openEavTables(EavTable[] tables, ExportWriter writer, XPath xpath) throws ExportException, IOException{
		eavParsers = new EavTableParser[tables.length];
		for( int i=0; i<eavParsers.length; i++ ){
			eavParsers[i] = tables[i].createParser(writer.openEAVTable(tables[i].getId()), xpath);
		}
	}
	protected FragmentExporter(XPath xpath, ExportDescriptor desc, ExportWriter writer) throws ExportException, XMLStreamException, ParserConfigurationException {
		super();
		
		try {
			patientParser = desc.getPatientTable().createParser(writer.openPatientTable(), xpath);
			visitParser = desc.getVisitTable().createParser(writer.openVisitTable(), xpath);
			// open eav parsers
			openEavTables(desc.getEAVTables(), writer, xpath);
			
		} catch (IOException e) {
			close( e::addSuppressed );
			throw new ExportException("Unable to open table for writing", e);
		} catch( ExportException e ){
			close( e::addSuppressed );
			throw e;
		}
		// initialise annotator
		factAnnotator = new FactClassAnnotator();
		for( ConceptGroup group : desc.getConcepts().getGroups() ){
			String clazz = group.getClazz();
			for( Concept concept : group.getConcepts() ){
				String s = concept.getNotation();
				if( s != null ){
					factAnnotator.addMapRule(s, clazz);
					continue;
				}
				s = concept.getWildcardNotation();
				if( s != null ){
					if( s.indexOf('*') < s.length()-1 ){
						throw new ExportException("Wildcard notation '"+s+"' must contain exactly one * at the end");
					}
					factAnnotator.addWildcardRule(s.substring(0, s.length()-1), clazz);
					continue;
				}
				
				throw new ExportException("Group concepts must have one of 'notation' or 'wildcard-notation' defined. Concept IRI not supported yet");
			}
		}
	}

	@Override
	protected void patientFragment(Element patient) throws ObservationException {
		currentPatient = patient;
		try {
			patientParser.processNode(patient);
		} catch (ExportException | IOException e) {
			throw new ObservationException(e);
		}
	}

	private void writeEavFacts(Element visit) throws ExportException, IOException{
		for( int i=0; i<eavParsers.length; i++ ){
			eavParsers[i].processNode(visit);
		}
	}
	@Override
	protected void visitFragment(Element visit) throws ObservationException {
		// annotate facts with class attribute
		factAnnotator.annotateFacts(visit.getChildNodes());
		// move visit to patient
		// this allows XPath expressions to access the patient via
		// the parent element. E.g. '../@id' to get the patient id
		currentPatient.appendChild(visit);
		try {
			visitParser.processNode(visit);
			writeEavFacts(visit);
		} catch (ExportException | IOException e) {
			throw new ObservationException(e);
		} finally {
			// remove visit from patient
			currentPatient.removeChild(visit);
		}
	}

	@Override
	public void close(){
		super.close();
		close( e -> reportError(new ObservationException(e)) );
	}
	private void close(Consumer<IOException> errorAction){
		if( patientParser != null ){
			try{
				patientParser.close();
			}catch( IOException e ){
				errorAction.accept(e);
			}
		}
		if( visitParser != null ){
			try{
				visitParser.close();
			}catch( IOException e ){
				errorAction.accept(e);
			}
		}
		if( eavParsers != null ){
			for( int i=0; i<eavParsers.length; i++ ){
				if( eavParsers[i] == null ){
					continue;
				}
				try{
					eavParsers[i].close();
				}catch( IOException e ){
					errorAction.accept(e);
				}
			}
		}
	}

	/**
	 * Retrieve number of processed patients
	 * @return patient count
	 */
	public int getPatientCount(){
		return patientParser.getRowCount();
	}
	/**
	 * Retrieve number of processed visits
	 * @return visit count
	 */
	public int getVisitCount(){
		return visitParser.getRowCount();
	}
}
