package javelin.controller.db.reader.factor;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Attack;
import javelin.model.unit.AttackSequence;

/**
 * @see FieldReader
 */
public class Attacks extends FieldReader {
	private final MonsterReader monsterReader;

	public Attacks(MonsterReader monsterReader, final String fieldname) {
		super(fieldname);
		this.monsterReader = monsterReader;
	}

	@Override
	public void read(final String value) {
		for (final String attackSequence : value.split(";")) {
			final AttackSequence sequence = check(value, attackSequence);
			if (sequence == null) {
				return;
			}
			for (final String attack : attackSequence.split(",")) {
				try {
					parseattack(attack, sequence);
				} catch (final StringIndexOutOfBoundsException e) {
					monsterReader.errorhandler
							.setInvalid("incorrect attack syntax");
					return;
				}
			}
		}
	}

	private void parseattack(final String attackp,
			final ArrayList<Attack> list) {
		String attack = attackp.replace("ranged", "").replace("melee", "")
				.replace("or", "").trim();
		final char numberOfAtks = attack.charAt(0);
		final int nOfAtks;
		if (Character.isDigit(numberOfAtks)) {
			nOfAtks = Integer.parseInt(Character.toString(numberOfAtks));
			attack = attack.substring(2).trim();
		} else {
			nOfAtks = 1;
		}
		final String name = attack.substring(0, attack.lastIndexOf(" "));
		checkprevious(list, name);
		for (final int bonus : parsebonuses(
				attack.substring(attack.lastIndexOf(" ") + 1))) {
			for (int i = 1; i <= nOfAtks; i++) {
				list.add(new Attack(name, bonus));
			}
		}
	}

	static Integer[] parsebonuses(final String bonusesP) {
		final String[] split = bonusesP.replaceAll(" ", "")
				.replaceAll("\\+", "").replace(",", ";").split("/");
		final Integer[] array =
				new ArrayList<Integer>().toArray(new Integer[split.length]);
		for (int i = 0; i < array.length; i++) {
			try {
				array[i] = Integer.parseInt(split[i].replace("+", " ").trim());
			} catch (final Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		return array;
	}

	/**
	 * TODO warns about similar names
	 */
	private void checkprevious(final ArrayList<Attack> list,
			final String name) {
		final HashSet<String> previousNamesSet = new HashSet<String>();
		for (final Attack a : list) {
			previousNamesSet.add(a.name);
		}
		for (final String previousName : previousNamesSet) {
			if (name.equalsIgnoreCase(previousName)
					|| name.equalsIgnoreCase(previousName + "s")
					|| (name + "s").equalsIgnoreCase(previousName)) {
				MonsterReader.log("WARNING adding attack '" + name
						+ "': Monster '" + monsterReader.monster
						+ "' already has similar-named attack ('" + previousName
						+ "'), this could confuse damage at this point. Please change the name.");
			}
		}
	}

	/**
	 * TODO can't have both attack types in a sequence, touch & repeating
	 */
	public AttackSequence check(final String value,
			final String attackSequence) {
		final boolean ismelee = attackSequence.contains("melee");
		final boolean isranged = attackSequence.contains("ranged");
		if (ismelee && isranged) {
			monsterReader.errorhandler.setInvalid(
					"Cannot have both types of attack in a sequence!");
			return null;
		}
		for (final String blackList : new String[] { "touch", "repeating" }) {
			if (value.toLowerCase().contains(blackList.toLowerCase())) {
				monsterReader.errorhandler.setInvalid(blackList);
				return null;
			}
		}
		final AttackSequence list = new AttackSequence();
		(ismelee ? monsterReader.monster.melee : monsterReader.monster.ranged)
				.add(list);
		return list;
	}
}