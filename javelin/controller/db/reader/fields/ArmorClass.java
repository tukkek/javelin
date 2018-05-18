package javelin.controller.db.reader.fields;

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
		String details = value.substring(value.indexOf("(") + 1,
				value.lastIndexOf(")"));
		for (String factor : details.split(",")) {
			factor = factor.trim();
			String type = factor.substring(factor.indexOf(" ") + 1);
			type = type.toLowerCase().trim();
			if ("size".equals(type) || "dex".equals(type)) {
				continue;
			}
			factor = factor.substring(0, factor.indexOf(" ")).trim();
			extra += Integer.parseInt(factor.replaceAll("\\+", ""));
		}
		reader.monster.armor = extra;
		int ac = Integer.parseInt(value.substring(0, value.indexOf(" ")));
		reader.monster.setrawac(ac);
	}
}