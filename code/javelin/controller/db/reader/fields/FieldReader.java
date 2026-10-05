package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;

import javelin.controller.db.reader.MonsterReader;

/**
 * Reads one of the XML tags inside a MONSTER tag.
 *
 * @see MonsterReader
 *
 * @author alex
 */
public abstract class FieldReader{
	/** XML tag. */
	public String fieldname="";
	/**
	 * Active reader. TODO change to MonsterReader#instance .
	 */
	protected MonsterReader reader;

	/**
	 * @param value Process the XML string of the field.
	 */
	public abstract void read(String value)
			throws NumberFormatException,PropertyVetoException;

	/** Constructor. */
	public FieldReader(MonsterReader reader,String fieldname){
		this.fieldname=fieldname;
		this.reader=reader;
	}
}