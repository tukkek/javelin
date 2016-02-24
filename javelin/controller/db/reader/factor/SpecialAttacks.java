package javelin.controller.db.reader.factor;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.SpecialtiesLog;
import javelin.controller.quality.Quality;

/**
 * @see FieldReader
 */
public class SpecialAttacks extends FieldReader {
	private final MonsterReader monsterReader;

	public SpecialAttacks(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	public void read(final String value) {
		int ignored = 0;
		String[] values = value.split(",");
		reading: for (final String attack : values) {
			final String trim = attack.trim();
			for (final Quality q : Quality.qualities) {
				if (attack.toLowerCase().contains(q.name)) {
					q.add(trim, monsterReader.monster);
					continue reading;
				}
			}
			ignored += 1;
			monsterReader.sAtks.add(trim);
		}
		SpecialtiesLog.log("    Special attacks: " + value + " (used "
				+ (values.length - ignored) + ")");
	}
}