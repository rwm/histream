package de.sekmi.histream.i2b2.ont;

class MetaEntry implements Cloneable{
	enum Type{
		Container, Folder, Leaf, Multiple
	}
	enum Visibility{
		ACTIVE, Disabled, Hidden
	}
	int level;
	String path;
	String label;
	boolean synonym;
	Type type;
	Visibility visibility;
	String basecode;
	String xml;
	String dimcode;
	String tooltip;
	String modpath;

	@Override
	public MetaEntry clone(){
		MetaEntry c = new MetaEntry();
		c.level = level;
		c.path = path;
		c.label = label;
		c.synonym = synonym;
		c.type = type;
		c.visibility = visibility;
		c.basecode = basecode;
		c.xml = xml;
		c.dimcode = dimcode;
		c.tooltip = tooltip;
		c.modpath = modpath;
		return c;
	}
}
