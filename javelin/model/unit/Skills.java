package javelin.model.unit;

import java.io.Serializable;

import javelin.controller.Weather;
import javelin.controller.ai.BattleAi;
import javelin.controller.upgrade.Spell;
import javelin.model.feat.Alertness;
import javelin.model.item.Scroll;
import javelin.model.item.Wand;

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
	/** {@link Monster#dexterity}-based. */
	public int acrobatics = 0;
	/** {@link Monster#constitution}-based. */
	public int concentration = 0;
	/**
	 * {@link Monster#charisma}-based. Opposed by {@link #perception}.
	 * 
	 * This is a better option than "bluff" because it doesn't require opposed
	 * Sense Motive checks, which would add a new skills largely useless for
	 * player units.
	 */
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
	public int search = 0;
	/** {@link Monster#intelligence}-based. */
	public int spellcraft = 0;
	/** {@link Monster#dexterity}-based. */
	public int stealth = 0;
	/** {@link Monster#wisdom}-based. */
	public int survival = 0;
	/** {@link Monster#charisma}-based. */
	public int usemagicdevice = 0;

	Monster monster;

	/**
	 * @param m
	 *            Unit whose skills are being tracked.
	 */
	public Skills(Monster m) {
		this.monster = m;
	}

	@Override
	protected Skills clone() throws CloneNotSupportedException {
		return (Skills) super.clone();
	}

	/**
	 * @return Like {@link #clone()} but also updates {@link #monster}.
	 */
	protected Skills clone(Monster m) {
		try {
			Skills s = clone();
			s.monster = m;
			return s;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return <code>true</code> if can decioher a {@link Spell} from a
	 *         {@link Scroll} or {@link Wand}.
	 */
	public boolean decipher(Spell s) {
		if (monster.skills.usemagicdevice() >= 15 + s.level) {
			return true;
		}
		return Math.max(Math.max(monster.intelligence, monster.wisdom),
				monster.charisma) + spellcraft / 2 > 10 + s.level;
	}

	/**
	 * @return Take-10 roll of {@link Skills#disabledevice}.
	 * @see Skills#take10(int, int)
	 */
	public int disable() {
		return Skills.take10(disabledevice, monster.intelligence);
	}

	/**
	 * @return a roll of {@link Skills#movesilently}.
	 */
	public int movesilently() {
		return Skills.take10(stealth, monster.dexterity);
	}

	/**
	 * Vision penalties here cut in half because they apply only to vision, not
	 * listening.
	 * 
	 * @param flyingbonus
	 *            <code>true</code> if flying creatures get a bonus for seeing
	 *            farther.
	 * @return Modified spot rank for given {@link Monster}.
	 */
	public int perceive(boolean flyingbonus) {
		int p = perception;
		if (flyingbonus && monster.fly > 0) {
			p += 1;
		}
		if (monster.hasfeat(Alertness.INSTANCE)) {
			p += Alertness.BONUS;
		}
		if (Weather.current != Weather.DRY) {
			p -= 4;
		}
		return p + monster.view() / 2;
	}

	/**
	 * @return A take 10 of {@link Skills#survival}.
	 */
	public int survive() {
		return Skills.take10(survival, monster.wisdom) - monster.view();
	}

	/**
	 * @return A take 10 of {@link Skills#usemagicdevice}.
	 */
	public int usemagicdevice() {
		return Skills.take10(usemagicdevice, monster.charisma);
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
}
