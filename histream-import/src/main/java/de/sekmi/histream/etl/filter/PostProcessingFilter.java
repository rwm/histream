package de.sekmi.histream.etl.filter;

import java.io.IOException;

import javax.xml.bind.annotation.XmlSeeAlso;

import de.sekmi.histream.scripting.AbstractFacts;

@XmlSeeAlso({DuplicateFactFilter.class, ScriptFilter.class})
public abstract class PostProcessingFilter {

	public abstract void processVisit(AbstractFacts facts)throws IOException;
}
