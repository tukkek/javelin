package javelin.controller.db.reader.fields;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.DamageEffect;
import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * @see FieldReader
 */
public class Damage extends FieldReader {
	static String[] ELEMENTALDAMAGE = new String[] { "fire", "cold",
			"electricity", "acid", "energy", "positive" };

	boolean nocrit = false;

	/** Constructor. */
	public Damage(MonsterReader reader, final String fieldname) {
		super(reader, fieldname);
	}

	@Override
	public void read(String value) {
		value = value.toLowerCase();
		for (final String blackList : new String[] { "transformation",
				"permanent", "corporeal instability", "mummy rot", "disease",
				"strand", "disruption", "slaying", "entangle", "stun",
				"implant", "energy drain", "wounding", "armor damage" }) {
			if (value.contains(blackList)) {
				reader.errorhandler.setInvalid(blackList + " damage");
				return;
			}
		}
		nocrit = false;
		parseattacks(value, getallattacks());
		if (nocrit) {
			MonsterReader.log(reader.monster + ": no critical damage data");
		}
	}

	void parseattacks(String value, final ArrayList<Attack> attacks) {
		for (String attack : value.replace(",", ";").split(";")) {
			if (!parseattacks(attacks, attack)) {
				if (reader.monster.toString().contains("Mohrg")) {
					System.out.println("#Mohrg");
				}
				reader.errorhandler.setInvalid("Attack not found: " + attack
						+ " (" + reader.monster + ") #damage");
			}
		}
	}

	boolean parseattacks(final ArrayList<Attack> attacks,
			final String attackp) {
		String attack = attackp.replace(" or", "").replace("or ", "")
				.replaceAll("  ", " ").trim();
		Spell effect = null;
		DamageEffect.init();
		for (DamageEffect e : DamageEffect.EFFECTS) {
			if (attack.contains(e.name)) {
				attack = removeeffect(attack, e.name);
				effect = e.spell.clone();
				effect.setdamageeffect();
				break;
			}
		}
		if (!attack.contains("(")) {
			nocrit = true;
			attack += " (20/x2)";
		}
		boolean found = false;
		for (final Attack a : attacks) {
			if (parseattack(attack, a)) {
				a.seteffect(effect);
				found = true;
			}
		}
		return found;
	}

	String removeeffect(String attack, String effect) {
		return attack.replace(effect, "").replaceAll("  ", " ").trim();
	}

	boolean parseattack(String attack, final Attack a) {
		String name = a.name;
		if (name.endsWith("s")) {
			name = name.substring(0, name.length() - 1);
		}
		if (attack.contains(name.toLowerCase())) {
			setdamage(attack, a);
			return true;
		} else {
			return false;
		}
	}

	void setdamage(String damage, final Attack a) {
		try {
			damage = parseelementaldamage(damage, a);
			a.damage = parsedamage(damage
					.substring(cutname(damage) + 1, damage.lastIndexOf(' '))
					.trim());
			a.getAverageDamageNoBonus();
			final String[] crit = damage
					.substring(damage.indexOf('(') + 1, damage.indexOf(')'))
					.split("/");
			a.threat = Integer.parseInt(crit[0]);
			a.multiplier = Integer.parseInt(crit[1].substring(1));
		} catch (final IndexOutOfBoundsException e) {
			reader.errorhandler.setInvalid("Invalid damage (oob): " + damage);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	String parseelementaldamage(String damage, final Attack a) {
		for (String element : ELEMENTALDAMAGE) {
			if (damage.contains(element)) {
				a.energy = true;
				return damage.replace(element, "").replace("  ", " ").trim();
			}
		}
		return damage;
	}

	int cutname(final String attack) {
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

	ArrayList<Attack> getallattacks() {
		final ArrayList<List<AttackSequence>> attacktypes = new ArrayList<List<AttackSequence>>();
		attacktypes.add(reader.monster.melee);
		attacktypes.add(reader.monster.ranged);
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