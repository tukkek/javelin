package javelin.controller.db.reader.fields;

import java.util.ArrayList;

import javelin.controller.db.reader.MonsterReader;
import javelin.controller.quality.Quality;

/**
 * Reads the <SpecialQualites> XML tag, using several {@link Quality} instances
 * to process them.
 */
public class SpecialQualities extends FieldReader {

	/** See {@link FieldReader#FieldReader(MonsterReader, String)}. */
	public SpecialQualities(MonsterReader monsterReader,
			final String fieldname) {
		super(monsterReader, fieldname);
	}

	@Override
	public void read(final String value) {
		// int ignored = 0;
		String[] values = value.split(",");
		ArrayList<String> qualities = new ArrayList<String>();
		reading: for (final String quality : values) {
			final String trim = quality.trim().toLowerCase();
			for (final Quality q : Quality.qualities) {
				if (q.apply(trim, reader.monster)) {
					q.add(trim, reader.monster);
					qualities.add(q.getClass().getSimpleName());
					continue reading;
				}
			}
			// ignored += 1;
			reader.debugqualities.add(trim);
		}
	}
}