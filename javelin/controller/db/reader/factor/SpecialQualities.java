package javelin.controller.db.reader.factor;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.db.reader.SpecialtiesLog;
import javelin.controller.quality.Quality;

/**
 * @see FieldReader
 */
public class SpecialQualities extends FieldReader {

	public SpecialQualities(MonsterReader monsterReader,
			final String fieldname) {
		super(monsterReader, fieldname);
	}

	@Override
	public void read(final String value) {
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