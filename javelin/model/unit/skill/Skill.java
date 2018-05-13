package javelin.model.unit.skill;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class Skill implements Serializable {
	/**
	 * TODO abilites should be handled like this skill redesign
	 */
	enum Ability {
		STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA
	}

	public static final HashMap<String, Skill> BYNAME = new HashMap<String, Skill>();
	public static final HashSet<Skill> ALL = new HashSet<Skill>();

	public static final Skill ACROBATICS = new Acrobatics();
	public static final Skill CONCENTRATION = new Concentration();
	public static final Skill DIPLOMACY = new Diplomacy();
	public static final Skill DISABLEDEVICE = new DisableDevice();
	public static final Skill DISGUISE = new Disguise();
	public static final Skill HEAL = new Heal();
	public static final Skill KNOWLEDGE = new Knowledge();
	public static final Skill PERCEPTION = new Perception();
	public static final Skill SPELLCRAFT = new Spellcraft();
	public static final Skill STEALTH = new Stealth();
	public static final Skill SURVIVAL = new Survival();
	public static final Skill USEMAGICDEVICE = new UseMagicDevice();

	public String name;
	public Ability ability;
	public boolean usedincombat = false;
	public boolean intelligent = false;

	Skill(String[] names, Ability a) {
		name = names[0].toLowerCase();
		ability = a;
		ALL.add(this);
		for (String name : names) {
			BYNAME.put(name.toLowerCase(), this);
		}
	}

	public Skill(String name, Ability a) {
		this(new String[] { name }, a);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Skill && name.equals(((Skill) o).name);
	}

	public int getbonus(Combatant c) {
		return getranks(c) + getabilitybonus(c.source) + c.skillmodifier;
	}

	int getabilitybonus(Monster m) {
		return Monster.getbonus(getabilityvalue(m));
	}

	public int getranks(Combatant c) {
		return getranks(c.source);
	}

	public int getranks(Monster m) {
		Integer ranks = m.ranks.get(name);
		return ranks == null ? 0 : ranks;
	}

	int getabilityvalue(Monster m) {
		if (ability == Ability.STRENGTH) {
			return m.strength;
		}
		if (ability == Ability.DEXTERITY) {
			return m.dexterity;
		}
		if (ability == Ability.CONSTITUTION) {
			return m.constitution;
		}
		if (ability == Ability.INTELLIGENCE) {
			return m.intelligence;
		}
		if (ability == Ability.WISDOM) {
			return m.wisdom;
		}
		if (ability == Ability.CHARISMA) {
			return m.charisma;
		}
		throw new RuntimeException("#unknownability " + ability);
	}

	/**
	 * @param ranks
	 *            Raises the number or tanks in this skill by this amount.
	 * @param m
	 *            Target unit.
	 * @see #setoriginal(int, Monster)
	 * @see #maximize(Monster)
	 */
	public void raise(int ranks, Monster m) {
		setranks(getranks(m) + ranks, m);
	}

	/**
	 * Used to set from {@link MonsterReader}. Will not overwrite previous
	 * lesser values.
	 *
	 * @param ranks
	 *            Final bonus as shown in stat block, with ability modifier.
	 * @param m
	 *            Target unit.
	 * @see #raise(int, Monster)
	 */
	public void setoriginal(int ranks, Monster m) {
		ranks -= getabilitybonus(m);
		if (ranks > getranks(m)) {
			setranks(ranks, m);
		}
	}

	void setranks(int ranks, Monster m) {
		m.ranks.put(name, ranks);
	}

	/**
	 * @param m
	 *            Raises the skill's ranks to its maximum value possible given
	 *            {@link Monster#hd}. Will not overwrite previously-higher
	 *            values.
	 */
	public void maximize(Monster m) {
		int max = m.hd.count() + 3;
		if (max > getranks(m)) {
			setranks(max, m);
		}
	}
}
