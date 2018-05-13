package javelin.model.unit.skill;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class Skill {
	/**
	 * TODO abilites should be handled like this skill redesign
	 */
	enum Ability {
		STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA
	}

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

	public static final Skill[] ALL = new Skill[] { ACROBATICS, CONCENTRATION,
			DIPLOMACY, DISABLEDEVICE, DISGUISE, HEAL, KNOWLEDGE, PERCEPTION,
			SPELLCRAFT, STEALTH, SURVIVAL, USEMAGICDEVICE };

	public String name;
	public int ranks = 0;
	public Ability ability;

	Skill(String name, Ability a) {
		this.name = name;
		ability = a;
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
		return getranks(c) + Monster.getbonus(getabilityvalue(c))
				+ c.skillmodifier;
	}

	public int getranks(Combatant c) {
		Integer ranks = c.source.ranks.get(name);
		return ranks == null ? 0 : ranks;
	}

	int getabilityvalue(Combatant c) {
		if (ability == Ability.STRENGTH) {
			return c.source.strength;
		}
		if (ability == Ability.DEXTERITY) {
			return c.source.dexterity;
		}
		if (ability == Ability.CONSTITUTION) {
			return c.source.constitution;
		}
		if (ability == Ability.INTELLIGENCE) {
			return c.source.intelligence;
		}
		if (ability == Ability.WISDOM) {
			return c.source.wisdom;
		}
		if (ability == Ability.CHARISMA) {
			return c.source.charisma;
		}
		throw new RuntimeException("#unknownability " + ability);
	}
}
