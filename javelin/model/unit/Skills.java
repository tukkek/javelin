package javelin.model.unit;

import java.io.Serializable;

import javelin.controller.ai.BattleAi;

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

	@Override
	protected Skills clone() throws CloneNotSupportedException {
		return (Skills) super.clone();
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
