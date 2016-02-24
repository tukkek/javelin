package javelin.controller.db.reader.factor;

import java.beans.PropertyVetoException;

import javelin.controller.db.reader.MonsterReader;

/**
 * @see FieldReader
 */
public class Initiative extends FieldReader {
	private final MonsterReader monsterReader;

	public Initiative(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	public void read(String value)
			throws NumberFormatException, PropertyVetoException {
		final int indexOf = value.indexOf("(");

		if (indexOf != -1) {
			value = value.substring(0, indexOf);
		}

		monsterReader.monster.initiative =
				Integer.parseInt(value.trim().replace("+", ""));
	}
}