package de.sekmi.histream.export.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({SequenceColumn.class, IdColumn.class})
public abstract class AbstractColumn {

	/**
	 * Column header name
	 */
	@XmlAttribute(required=true)
	String header;


	/**
	 * Column value to use to indicate NA.
	 * This will be used if null values are
	 * encountered by the column. If not specified,
	 * the empty string {@code ''} will be used.
	 */
	@XmlAttribute
	String na;
	
	/**
	 * Prepare the column for the next row. This
	 * method is called once for every row. 
	 * <p> The default implementation does nothing.</p>
	 */
	protected void prepareRow(){
		
	}
	
	/**
	 * Initializes the column. This method will be called
	 * only once and before any other method.
	 */
	protected void initialize(){
		
	}
	
	/**
	 * Get the column value for the current row.
	 * @return String value or {@code null} for NA.
	 */
	public abstract Object getValueString();
}
