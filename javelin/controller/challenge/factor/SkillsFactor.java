package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.skill.Acrobatics;
import javelin.controller.upgrade.skill.Concentration;
import javelin.controller.upgrade.skill.Diplomacy;
import javelin.controller.upgrade.skill.DisableDevice;
import javelin.controller.upgrade.skill.Disguise;
import javelin.controller.upgrade.skill.GatherInformation;
import javelin.controller.upgrade.skill.Heal;
import javelin.controller.upgrade.skill.Knowledge;
import javelin.controller.upgrade.skill.Perception;
import javelin.controller.upgrade.skill.Search;
import javelin.controller.upgrade.skill.Spellcraft;
import javelin.controller.upgrade.skill.Stealth;
import javelin.controller.upgrade.skill.Survival;
import javelin.controller.upgrade.skill.UseMagicDevice;
import javelin.model.unit.Monster;
import javelin.model.unit.Skills;

/**
 * Calculates a challenge rating value based on the skills a character has. To
 * do that we remove skill points from {@link ClassLevelFactor} and
 * {@link HdFactor}. Since if a creature acquires a character class it follows
 * the rules for multi-class characters then we ignore the 4x bonus points of
 * first level..
 * 
 * All skills are being considered class skills.
 * 
 * @author alex
 */
public class SkillsFactor extends CrFactor {
	/** Challenge rating cost per skill rank. */
	public static float COST = .02f;

	@Override
	public float calculate(Monster m) {
		Skills skills = m.skills;
		return COST * (m.skillpool + skills.concentration + skills.diplomacy
				+ skills.disabledevice + skills.gatherinformation
				+ skills.stealth + skills.knowledge + +skills.search
				+ skills.spellcraft + skills.perception + skills.acrobatics
				+ skills.survival + skills.usemagicdevice);
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.good.add(Diplomacy.SINGLETON);
		handler.good.add(GatherInformation.SINGLETON);

		handler.evil.add(Stealth.SINGLETON);
		handler.evil.add(Disguise.SINGLETON);

		handler.water.add(Knowledge.SINGLETON);
		handler.water.add(Concentration.SINGLETON);
		handler.water.add(Heal.SINGLETON);

		handler.wind.add(Perception.SINGLETON);
		handler.wind.add(Acrobatics.SINGLETON);
		handler.wind.add(DisableDevice.SINGLETON);

		handler.earth.add(Survival.SINGLETON);

		handler.magic.add(Search.SINGLETON);
		handler.magic.add(Spellcraft.SINGLETON);
		handler.magic.add(UseMagicDevice.SINGLETON);
	}

	/**
	 * @param progression
	 *            How many skills are gained per class level or monster hit die.
	 * @param monster
	 *            Applies {@link Monster#intelligence} to sum
	 * @return the challenge rating value for how much should be gained in
	 *         skills each level.
	 */
	public static float levelupcost(int progression, Monster m) {
		return levelup(progression, m) * COST;
	}

	/**
	 * Same as {@link #levelupcost(int, Monster)} but returns skill points
	 * instead of challenge rating.
	 */
	public static int levelup(int progression, Monster m) {
		return Math.max(1, progression + Monster.getbonus(m.intelligence));
	}
}
