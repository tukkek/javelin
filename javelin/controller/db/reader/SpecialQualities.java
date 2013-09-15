package javelin.controller.db.reader;

import javelin.controller.db.SpecialtiesLog;
import javelin.controller.quality.Quality;

class SpecialQualities extends FieldReader {

	SpecialQualities(MonsterReader monsterReader, final String fieldname) {
		super(monsterReader, fieldname);
	}

	@Override
	void read(final String value) {
		int ignored = 0;
		String[] values = value.split(",");
		reading: for (final String quality : values) {
			final String trim = quality.trim();
			for (final Quality q : Quality.qualities) {
				if (quality.toLowerCase().contains(q.name)) {
					q.add(trim, reader.monster);
					continue reading;
				}
			}
			ignored += 1;
			reader.debugqualities.add(trim);
		}
		SpecialtiesLog.log("    Special qualities: " + value + " (used "
				+ (values.length - ignored) + ")");
	}
}