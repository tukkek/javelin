package javelin.controller.challenge.factor;

import java.util.TreeSet;

import javelin.controller.upgrade.SkillUpgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.unit.Monster;
import javelin.model.unit.skill.Skill;

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
		int ranks = 0;
		for (int skill : m.ranks.values()) {
			ranks += skill;
		}
		return COST * ranks;
	}

	@Override
	public void registerupgrades(UpgradeHandler handler) {
		handler.good.add(new SkillUpgrade(Skill.DIPLOMACY));
		handler.good.add(new SkillUpgrade(Skill.SENSEMOTIVE));

		handler.evil.add(new SkillUpgrade(Skill.STEALTH));
		handler.evil.add(new SkillUpgrade(Skill.DISGUISE));
		handler.evil.add(new SkillUpgrade(Skill.BLUFF));

		handler.water.add(new SkillUpgrade(Skill.KNOWLEDGE));
		handler.water.add(new SkillUpgrade(Skill.CONCENTRATION));
		handler.water.add(new SkillUpgrade(Skill.HEAL));

		handler.wind.add(new SkillUpgrade(Skill.PERCEPTION));
		handler.wind.add(new SkillUpgrade(Skill.ACROBATICS));
		handler.wind.add(new SkillUpgrade(Skill.DISABLEDEVICE));

		handler.earth.add(new SkillUpgrade(Skill.SURVIVAL));

		handler.magic.add(new SkillUpgrade(Skill.SPELLCRAFT));
		handler.magic.add(new SkillUpgrade(Skill.USEMAGICDEVICE));
	}

	/**
	 * @param progression
	 *            How many skills are gained per class level or monster hit die.
	 * @param m
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

	@Override
	public String log(Monster m) {
		TreeSet<String> skills = new TreeSet<String>();
		for (String skill : m.ranks.keySet()) {
			skills.add(skill);
		}
		return skills.toString();
	}
}
