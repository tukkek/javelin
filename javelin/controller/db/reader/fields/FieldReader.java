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
public abstract class FieldReader {
	public String fieldname = "";
	protected MonsterReader reader;

	public abstract void read(String value)
			throws NumberFormatException, PropertyVetoException;

	public FieldReader(MonsterReader reader, String fieldname) {
		this.fieldname = fieldname;
		this.reader = reader;
	}
}