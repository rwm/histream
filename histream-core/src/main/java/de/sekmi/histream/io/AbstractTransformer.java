package de.sekmi.histream.io;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import de.sekmi.histream.Observation;

public class AbstractTransformer {
	final protected Queue<Observation> fifo;
	final protected Consumer<Observation> fifoPush;
	final protected Transformation transformation;
	
	protected AbstractTransformer(Transformation transformation){
		this.transformation = transformation;
		this.fifo = new LinkedList<Observation>();
		this.fifoPush = fifo::add;
	}

}
