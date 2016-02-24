package javelin.controller.db.reader.factor;

import javelin.controller.db.reader.MonsterReader;

/**
 * @see FieldReader
 */
public class HitDice extends FieldReader {
	private final MonsterReader monsterReader;

	public HitDice(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	public void read(final String value) {
		String[] split = value.toLowerCase().split("d");
		final String hdS = split[0].trim();
		final boolean positive = split[1].contains("+");
		split = split[1].split(positive ? "\\+" : "\\-");
		int bonus = split.length >= 2 ? Integer.parseInt(trim(split[1])) : 0;
		if (!positive) {
			bonus *= -1;
		}
		final float hd;
		if ("1/2".equals(hdS)) {
			hd = .5f;
		} else if ("1/4".equals(hdS)) {
			hd = .25f;
		} else {
			hd = Float.parseFloat(hdS);
		}
		monsterReader.monster.originalhd = hd;
		monsterReader.monster.hd.add(hd, Integer.parseInt(trim(split[0])),
				bonus);
	}

	public String trim(final String bonusstr) {
		final int i = bonusstr.indexOf(' ');
		return i == -1 ? bonusstr : bonusstr.substring(0, i);
	}
}