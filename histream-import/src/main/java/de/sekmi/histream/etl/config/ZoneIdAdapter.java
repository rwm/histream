package de.sekmi.histream.etl.config;

import java.time.ZoneId;

import javax.xml.bind.annotation.adapters.XmlAdapter;

// XXX unused
public class ZoneIdAdapter extends XmlAdapter<String, ZoneId> {

	@Override
	public String marshal(ZoneId arg0) throws Exception {
		return arg0.getId();
	}

	@Override
	public ZoneId unmarshal(String arg0) throws Exception {
		return ZoneId.of(arg0);
	}

}
