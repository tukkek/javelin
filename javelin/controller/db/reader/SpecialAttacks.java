package javelin.controller.db.reader;

import javelin.controller.db.SpecialtiesLog;

class SpecialAttacks extends FieldReader {
	/**
	 * 
	 */
	private final MonsterReader monsterReader;

	SpecialAttacks(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	void read(final String value) {
		int ignored = 0;
		String[] values = value.split(",");
		for (final String sAtk : values) {
			this.monsterReader.sAtks.add(sAtk.trim());
			/**
			 * TODO when reading special attacks use same logic as
			 * SpecialQualities
			 */
			ignored += 1;
		}
		SpecialtiesLog.log("    Special attacks: " + value + " (used "
				+ (values.length - ignored) + ")");
	}
}