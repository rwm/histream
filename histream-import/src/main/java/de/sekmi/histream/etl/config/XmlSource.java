package de.sekmi.histream.etl.config;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmlSource {

	@XmlAttribute
	URL url;
	@XmlElement
	Transform[] transform;
	
	public static class Transform{
		@XmlAttribute
		URL with;
		@XmlAttribute
		String to; // TODO use Path internally
		
		public Transform(){
		}
		public Transform(String with, String to) throws MalformedURLException{
			this.with = new URL(with);
			this.to = to;
		}
	}
}
