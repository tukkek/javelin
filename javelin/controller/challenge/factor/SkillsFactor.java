package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.skill.Acrobatics;
import javelin.controller.upgrade.skill.Concentration;
import javelin.controller.upgrade.skill.Diplomacy;
import javelin.controller.upgrade.skill.DisableDevice;
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
		int ranks = skills.concentration + skills.diplomacy
				+ skills.disabledevice + skills.gatherinformation
				+ skills.stealth + skills.knowledge + +skills.search
				+ skills.spellcraft + skills.perception + skills.acrobatics
				+ skills.survival + skills.usemagicdevice;
		return ranks * COST;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {

		handler.good.add(new Diplomacy());
		handler.good.add(new GatherInformation());

		handler.evil.add(new Stealth());

		handler.water.add(new Knowledge());
		handler.water.add(new Concentration());
		handler.water.add(new Heal());

		handler.wind.add(new Perception());
		handler.wind.add(new Acrobatics());
		handler.wind.add(new DisableDevice());

		handler.earth.add(new Survival());

		handler.magic.add(new Search());
		handler.magic.add(new Spellcraft());
		handler.magic.add(new UseMagicDevice());
	}

	/**
	 * @param progression
	 *            How many skills are gained per class level or monster hit die.
	 * @param monster
	 *            Applies {@link Monster#intelligence} to sum
	 * @return the challenge rating value for how much should be gained in
	 *         skills each level.
	 */
	public static float levelup(int progression, Monster m) {
		return Math.max(1, progression + Monster.getbonus(m.intelligence))
				* COST;
	}
}
