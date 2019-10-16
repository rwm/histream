package de.sekmi.histream.etl.config;

import java.time.temporal.ChronoUnit;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ChronoPrecisionAdapter extends XmlAdapter<String, ChronoUnit>{
	@Override
	public String marshal(ChronoUnit arg0) throws Exception {
		if( arg0 == null ) {
			return null;
		}
		return arg0.name();
	}

	@Override
	public ChronoUnit unmarshal(String arg0) throws Exception {
		if( arg0 == null ) {
			return null;
		}
		return ChronoUnit.valueOf(arg0);
	}

}
