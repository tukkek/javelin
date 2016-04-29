package javelin.controller.challenge.factor;

import javelin.controller.upgrade.UpgradeHandler;
import javelin.controller.upgrade.skill.Acrobatics;
import javelin.controller.upgrade.skill.Concentration;
import javelin.controller.upgrade.skill.Diplomacy;
import javelin.controller.upgrade.skill.DisableDevice;
import javelin.controller.upgrade.skill.GatherInformation;
import javelin.controller.upgrade.skill.Hide;
import javelin.controller.upgrade.skill.Knowledge;
import javelin.controller.upgrade.skill.Listen;
import javelin.controller.upgrade.skill.MoveSilently;
import javelin.controller.upgrade.skill.Search;
import javelin.controller.upgrade.skill.Spellcraft;
import javelin.controller.upgrade.skill.Spot;
import javelin.controller.upgrade.skill.Survival;
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
				+ skills.disabledevice + skills.gatherinformation + skills.hide
				+ skills.knowledge + skills.listen + skills.movesilently
				+ skills.search + skills.spellcraft + skills.spot
				+ skills.acrobatics + skills.survival;
		return ranks * COST;
	}

	@Override
	public void listupgrades(UpgradeHandler handler) {
		handler.evil.add(new Hide("Hide"));

		handler.good.add(new Diplomacy("Diplomacy"));

		handler.water.add(new Knowledge("Knowledge"));

		handler.wind.add(new Spot("Spot"));
		handler.wind.add(new Listen("Listen"));

		handler.earth.add(new GatherInformation("Gather information"));
		handler.earth.add(new Survival("Survival"));
		handler.earth.add(new Concentration("Concentration"));

		handler.wind.add(new MoveSilently("Move silently"));
		handler.wind.add(new Search("Search"));
		handler.wind.add(new DisableDevice("Disable device"));
		handler.wind.add(new Acrobatics("Acrobatics"));

		handler.magic.add(new Spellcraft("Spellcraft"));
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
