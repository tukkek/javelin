package javelin.controller.db.reader;

import java.util.ArrayList;
import java.util.List;

import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;

class Damage extends FieldReader {
	private final MonsterReader monsterReader;

	Damage(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	void read(final String value) {
		// TODO implement
		for (final String blackList : new String[] { "poison",
				"transformation", "permanent", "fire", "corporeal instability",
				"acid", "cold", "mummy rot", "disease", "energy", "strand",
				"disruption", "slaying", "entangle", "fear", "stun",
				"paralysis", "implant" }) {
			if (value.toLowerCase().contains(blackList.toLowerCase())) {
				monsterReader.errorhandler.setInvalid(blackList);
				return;
			}
		}
		final ArrayList<Attack> attacks = getallattacks();
		for (String attack : value.replace(",", ";").split(";")) {
			Boolean found = parseattack(attacks, attack);
			if (found == null) {
				return;
			}
			if (!found) {
				monsterReader.errorhandler
						.setInvalid("Attack not found for damage");
			}
		}
	}

	public Boolean parseattack(final ArrayList<Attack> attacks,
			final String attackp) {
		// if (monsterReader.monster.name.equals("Grick")) {
		// System.out.println("#breakitdown");
		// }
		boolean found = false;
		String attack = attackp.replace("or", "").trim();
		for (final Attack a : attacks) {
			final int namelimit = cutname(attack);
			if (namelimit == -1) {
				monsterReader.errorhandler.setInvalid("No crit data");
				return null;
			}
			final String name = attack.substring(0, namelimit);
			if (attack.toLowerCase().contains("wounding")) {
				monsterReader.errorhandler
						.setInvalid("Special damage: wounding");
				return null;
			}
			if (Character.isDigit(attack.charAt(0))) {
				attack = attack.substring(attack.indexOf(' ')).trim();
			}
			/**
			 * TODO some attacks have similar names, but different damage (for
			 * example, a 'club' attack could be used with one or two hands,
			 * with varying Str modifier.
			 * 
			 * The ideal would be to review all monsters.xml , but for now, we
			 * could only warn while loading.
			 */
			if (a.name.equalsIgnoreCase(name)
					|| a.name.equalsIgnoreCase(name + "s")) {
				setdamage(attack, a);
				found = true;
			}
		}
		return found;
	}

	public void setdamage(final String attack, final Attack a) {
		try {
			a.damage = parsedamage(attack.substring(cutname(attack) + 1,
					attack.lastIndexOf(' ')).trim());
			a.getAverageDamageNoBonus();
			final String[] crit = attack.substring(attack.indexOf('(') + 1,
					attack.indexOf(')')).split("/");
			a.threat = Integer.parseInt(crit[0]);
			a.multiplier = Integer.parseInt(crit[1].substring(1));
		} catch (final IndexOutOfBoundsException e) {
			monsterReader.errorhandler.setInvalid("Invalid damage");
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int cutname(final String attack) {
		for (int i = attack.lastIndexOf(' ') - 1; i >= 0; i--) {
			if (attack.charAt(i) == ' ') {
				return i;
			}
		}
		return -1;
	}

	static private int[] parsedamage(final String damage) {
		final String[] beforeD = damage.replaceAll(" ", "").split("d");
		final String afterD = beforeD[1];
		final String sides;
		final String bonus;
		if (afterD.contains("+")) {
			final String[] split = afterD.split("\\+");
			sides = split[0];
			bonus = split[1];
		} else if (afterD.contains("-")) {
			final String[] split = afterD.split("-");
			sides = split[0];
			bonus = "-" + split[1];
		} else {
			sides = beforeD[1];
			bonus = "0";
		}
		return new int[] { Integer.parseInt(beforeD[0]),
				Integer.parseInt(sides), Integer.parseInt(bonus) };
	}

	public ArrayList<Attack> getallattacks() {
		final ArrayList<List<AttackSequence>> attacktypes = new ArrayList<List<AttackSequence>>();
		attacktypes.add(monsterReader.monster.melee);
		attacktypes.add(monsterReader.monster.ranged);
		final ArrayList<Attack> attacks = new ArrayList<Attack>();
		for (final List<AttackSequence> attacktype : attacktypes) {
			for (final List<Attack> thisattack : attacktype) {
				for (final Attack attack : thisattack) {
					attacks.add(attack);
				}
			}
		}
		return attacks;
	}
}