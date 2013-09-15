package javelin.controller.db.reader;

import java.beans.PropertyVetoException;


public class Paragraph extends FieldReader {

	/**
	 * 
	 */
	private final MonsterReader monsterReader;

	public Paragraph(MonsterReader monsterReader, String string) {
		super(string);
		this.monsterReader = monsterReader;
	}

	@Override
	void read(String value) throws NumberFormatException,
			PropertyVetoException {
		this.monsterReader.describe(value);
	}

}