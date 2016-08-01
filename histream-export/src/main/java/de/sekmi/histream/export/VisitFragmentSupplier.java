package de.sekmi.histream.export;

import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.sekmi.histream.Observation;
import de.sekmi.histream.ObservationSupplier;

public class VisitFragmentSupplier implements Supplier<Node> {

	private ObservationSupplier supplier;
	private Node visit;
	private VisitFragmentParser parser;
	private boolean closed;
	
	public VisitFragmentSupplier(ObservationSupplier supplier) throws XMLStreamException, ParserConfigurationException {
		this.supplier = supplier;
		this.parser = new VisitFragmentParser() {
			@Override
			protected void visitFragment(Element visit) {
				VisitFragmentSupplier.this.visit = visit;
			}
		};
	}
	
	@Override
	public Node get() {
		if( closed ){
			return null;
		}
		// feed facts until we get a visit fragment
		while( visit == null ){
			Observation o = supplier.get();
			if( o == null ){
				// end of stream
				// might get another visit with the close
				parser.close();
				closed = true;
				break;
			}
			parser.accept(o);
		}
		if( visit == null ){
			// end of stream
		}
		Node local = visit;
		// clear member variable for next call
		this.visit = null;
		return local;
	}

}
