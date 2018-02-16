package de.sekmi.histream.etl.config;

import javax.xml.bind.annotation.XmlElement;

import de.sekmi.histream.etl.filter.PostProcessingFilter;

public class PostProcessing {
	@XmlElement
	PostProcessingFilter[] filter;

}
