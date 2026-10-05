package javelin.controller.db.reader.fields;

import java.beans.PropertyVetoException;

import javelin.controller.db.reader.MonsterReader;

/**
 * @see FieldReader
 */
public class Initiative extends FieldReader{

	public Initiative(MonsterReader reader,final String fieldname){
		super(reader,fieldname);
	}

	@Override
	public void read(String value)
			throws NumberFormatException,PropertyVetoException{
		final int indexOf=value.indexOf("(");

		if(indexOf!=-1) value=value.substring(0,indexOf);

		reader.monster.initiative=Integer.parseInt(value.trim().replace("+",""));
	}
}