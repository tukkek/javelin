package javelin.model.unit;

import java.io.Serializable;

import javelin.Javelin;
import javelin.controller.Weather;
import javelin.controller.ai.BattleAi;
import javelin.model.item.Scroll;
import javelin.model.item.Wand;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.feat.skill.Alertness;
import javelin.model.unit.feat.skill.Deceitful;

/**
 * Keeps track of skill ranks of a certain {@link Monster}.
 * 
 * Usually instead of rolling the skills as usual most of the time it's implied
 * a take 10 is being used, by various reasons, such as prevent scumming (trying
 * the same action many times until you get a high roll) and having to use the
 * random number generator inside {@link BattleAi}.
 * 
 * Synergy bonuses are not being used for now. See doc/skills.txt guide for more
 * information.
 * 
 * TODO it would probably be better to have this as an Enum,Integer Map. This
 * way would allow more programmatic freedom on stuff like
 * {@link javelin.controller.db.reader.fields.Skills}.
 * 
 * @author alex
 */
public class Skills implements Serializable, Cloneable {
	/**
	 * {@link Monster#dexterity}-based.
	 * 
	 * @see Monster#tumble()
	 */
	@Deprecated
	public int acrobatics = 0;
	/** {@link Monster#constitution}-based. */
	@Deprecated
	public int concentration = 0;
	/**
	 * {@link Monster#charisma}-based. Opposed by {@link #perception}.
	 * 
	 * This is a better option than "bluff" because it doesn't require opposed
	 * Sense Motive checks, which would add a new skills largely useless for
	 * player units.
	 */
	@Deprecated
	public int disguise = 0;
	/** {@link Monster#charisma}-based. */
	public int diplomacy = 0;
	/** {@link Monster#intelligence}-based. */
	public int disabledevice = 0;
	/** {@link Monster#charisma}-based. */
	public int gatherinformation = 0;
	/** {@link Monster#wisdom} based. */
	public int heal = 0;
	/**
	 * {@link Monster#intelligence}-based. Represents the d20 skill Area
	 * Knowledge.
	 */
	public int knowledge = 0;
	/**
	 * {@link Monster#wisdom}-based.
	 * 
	 * @see #perceive(Monster)
	 */
	@Deprecated
	public int perception = 0;
	/** {@link Monster#intelligence}-based. */
	public int spellcraft = 0;
	/** {@link Monster#dexterity}-based. */
	public int stealth = 0;
	/** {@link Monster#wisdom}-based. */
	public int survival = 0;
	/** {@link Monster#charisma}-based. */
	public int usemagicdevice = 0;

	@Override
	protected Skills clone() throws CloneNotSupportedException {
		return (Skills) super.clone();
	}

	/**
	 * @return <code>true</code> if can decioher a {@link Spell} from a
	 *         {@link Scroll} or {@link Wand}.
	 */
	public boolean decipher(Spell s, Monster monster) {
		if (usemagicdevice(monster) >= 15 + s.level) {
			return true;
		}
		if (!monster.think(-2)) {
			return false;
		}
		return Math.max(Math.max(monster.intelligence, monster.wisdom),
				monster.charisma) + spellcraft / 2 >= 10 + s.level;
	}

	/**
	 * @return Take-10 roll of {@link Skills#disabledevice}.
	 * @see Skills#take10(int, int)
	 */
	public int disable(Monster monster) {
		return Skills.take10(disabledevice, monster.intelligence);
	}

	/**
	 * @return a roll of {@link Skills#movesilently}.
	 */
	public int movesilently(Monster monster) {
		return Skills.take10(stealth, monster.dexterity);
	}

	/**
	 * Vision penalties here cut in half because they apply only to vision, not
	 * listening.
	 * 
	 * @param flyingbonus
	 *            <code>true</code> if flying creatures get a bonus for seeing
	 *            farther.
	 * @param periodpenalty
	 *            Penalty according to {@link Monster#vision} and
	 *            {@link Javelin#getDayPeriod()}.
	 * @return Modified spot rank for given {@link Monster} - doesn't include
	 *         widsom bonus or dice roll.
	 * @see Skills#take10(int, int)
	 */
	public int perceive(boolean flyingbonus, boolean weatherpenalty,
			boolean periodpenalty, Monster monster) {
		int p = perception;
		if (monster.hasfeat(Alertness.SINGLETON)) {
			int bonus = Alertness.BONUS;
			if (p >= 10) {
				bonus += 2;
			}
			p += bonus;
		}
		if (flyingbonus && monster.fly > 0) {
			p += 2;
		}
		if (weatherpenalty && Weather.current != Weather.DRY) {
			p += Weather.current == Weather.STORM ? -4 : -2;
		}
		if (periodpenalty) {
			p += monster.see();
		}
		return p;
	}

	/**
	 * @return A take 10 of {@link Skills#survival}.
	 */
	public int survive(Monster m) {
		return Skills.take10(survival, m.wisdom) - m.see();
	}

	/**
	 * @return A take 10 of {@link Skills#usemagicdevice}.
	 */
	public int usemagicdevice(Monster monster) {
		return Skills.take10(usemagicdevice, monster.charisma);
	}

	/**
	 * Since we don't have bluff bump the base bonus from {@link Deceitful} to
	 * +3.
	 * 
	 * @return {@link #disguise} take 10.
	 */
	public int disguise(Monster monster) {
		int roll = Skills.take10(disguise, monster.charisma);
		if (monster.hasfeat(Deceitful.SINGLETON)) {
			roll += disguise >= 10 ? 4 : 3;
		}
		return roll;
	}

	/**
	 * @param skill
	 *            Given a skill ranking like {@link #concentration}...
	 * @param attribute
	 *            and it's governing attribute...
	 * @return the sum of the rank with the attribute bonus.
	 */
	static public int take10(int skill, int attribute) {
		return 10 + skill + Monster.getbonus(attribute);
	}

	/**
	 * @return The given number of ranks with a proper + or - before.
	 */
	public static String signed(int ranks) {
		return ranks >= 0 ? "+" + ranks : Integer.toString(ranks);
	}

	/**
	 * TODO this is terrible to maintain because it' seasy to forget, making
	 * Skill enum-based should solve this
	 */
	public void changeall(int bonus) {
		acrobatics += bonus;
		concentration += bonus;
		diplomacy += bonus;
		disabledevice += bonus;
		disguise += bonus;
		gatherinformation += bonus;
		heal += bonus;
		knowledge += bonus;
		perception += bonus;
		spellcraft += bonus;
		stealth += bonus;
		survival += bonus;
		usemagicdevice += bonus;
	}
}
