package javelin.controller.db.reader.factor;

import java.beans.PropertyVetoException;

import javelin.controller.db.reader.MonsterReader;

/**
 * Reads a P tag.
 * 
 * @see FieldReader
 */
public class Paragraph extends FieldReader {
	private final MonsterReader monsterReader;

	public Paragraph(MonsterReader monsterReader, String string) {
		super(string);
		this.monsterReader = monsterReader;
	}

	@Override
	public void read(String value)
			throws NumberFormatException, PropertyVetoException {
		this.monsterReader.describe(value);
	}

}