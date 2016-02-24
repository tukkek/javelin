package javelin.controller.db.reader.factor;

import javelin.controller.db.reader.MonsterReader;

/**
 * @see FieldReader
 */
public class ArmorClass extends FieldReader {

	public ArmorClass(MonsterReader monsterReader, final String fieldname) {
		super(monsterReader, fieldname);
	}

	@Override
	public void read(final String value) {
		int extra = 0;
		for (final String acFactor : value
				.substring(value.indexOf("(") + 1, value.lastIndexOf(")"))
				.split(",")) {

			final String trim = acFactor.trim();
			final String type =
					trim.substring(trim.indexOf(" ")).toLowerCase().trim();

			if ("size".equals(type) || "dex".equals(type)) {
				continue;
			}

			extra += Integer.parseInt(trim.substring(0, trim.indexOf(" "))
					.trim().replaceAll("\\+", ""));
		}
		reader.monster.armor = extra;
		reader.monster.ac =
				Integer.parseInt(value.substring(0, value.indexOf(" ")));
	}
}