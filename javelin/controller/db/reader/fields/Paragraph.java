package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;

import javelin.controller.db.reader.MonsterReader;

/**
 * Reads a P tag.
 *
 * @see FieldReader
 */
public class Paragraph extends FieldReader{

	public Paragraph(MonsterReader reader,String string){
		super(reader,string);
	}

	@Override
	public void read(String value)
			throws NumberFormatException,PropertyVetoException{
		reader.describe(value);
	}

}