package javelin.controller.db.reader;

import java.beans.PropertyVetoException;

class Initiative extends FieldReader {
	/**
	 * 
	 */
	private final MonsterReader monsterReader;

	Initiative(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	void read(String value) throws NumberFormatException, PropertyVetoException {
		final int indexOf = value.indexOf("(");

		if (indexOf != -1) {
			value = value.substring(0, indexOf);
		}

		monsterReader.monster.initiative = Integer.parseInt(value.trim()
				.replace("+", ""));
	}
}