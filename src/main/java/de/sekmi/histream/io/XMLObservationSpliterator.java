package de.sekmi.histream.io;

import java.io.InputStream;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import de.sekmi.histream.Observation;

public class XMLObservationSpliterator implements Spliterator<Observation>{
	private XMLStreamReader reader;
	
	public XMLObservationSpliterator(XMLStreamReader reader) {
		this.reader = reader;
	}
	public XMLObservationSpliterator(InputStream input) throws XMLStreamException, FactoryConfigurationError {
		this(XMLInputFactory.newInstance().createXMLStreamReader(input));
	}
	@Override
	public boolean tryAdvance(Consumer<? super Observation> action) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Spliterator<Observation> trySplit() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long estimateSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int characteristics() {
		// TODO Auto-generated method stub
		return 0;
	}

}
