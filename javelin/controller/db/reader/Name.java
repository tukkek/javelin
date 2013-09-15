package javelin.controller.db.reader;

import java.beans.PropertyVetoException;

import javelin.controller.db.SpecialtiesLog;

class Name extends FieldReader {
	/**
	 * 
	 */
	private final MonsterReader monsterReader;

	Name(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	void read(final String value) throws PropertyVetoException {
		final int typeSeparator = value.indexOf(":");
		final int reverseSeparator;
		String name;
		String type;

		if (typeSeparator == -1) {
			name = value;
			type = "";
		} else {
			name = value.substring(typeSeparator + 1);
			type = value.substring(0, typeSeparator);
		}

		reverseSeparator = name.indexOf(",");
		if (reverseSeparator > 0) {
			name = name.substring(reverseSeparator + 2) + " "
					+ name.substring(0, reverseSeparator);
		}
		name = name.substring(0, 1).toUpperCase()
				+ name.substring(1).toLowerCase();
		monsterReader.monster.name = name;
		monsterReader.monster.group = type;
		SpecialtiesLog.log(name);
	}
}