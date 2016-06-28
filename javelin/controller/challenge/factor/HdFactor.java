/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;

/**
 * Can't simply upgrade HD since it gives more base attack bonus, abilities...
 * (feats are taken into consideration by FeatsFactor)
 * 
 * @see CrFactor
 * 
 * @author alex
 */
public class HdFactor extends CrFactor {
	@Override
	public float calculate(final Monster monster) {
		final String type = monster.type.toLowerCase();
		float crByDie;
		if (type.contains("dragon")) {
			crByDie = .75f - SkillsFactor.levelup(6, monster);
		} else if (type.contains("outsider")) {
			crByDie = .7f - SkillsFactor.levelup(8, monster);
		} else if (type.contains("magical beast") || type.contains("beast")
				|| type.contains("shapechanger")) {
			/* beast is a typo */
			crByDie = .65f - SkillsFactor.levelup(2, monster);
		} else if (type.contains("monstrous humanoid")) {
			crByDie = .6f - SkillsFactor.levelup(2, monster);
		} else if (type.contains("aberration") || type.contains("animal")
				|| type.contains("elemental") || type.contains("giant")
				|| type.contains("humanoid") || andIntelligent("plant", monster)
				|| andIntelligent("vermin", monster)) {
			crByDie = .55f - SkillsFactor.levelup(2, monster);
		} else if (type.contains("fey")) {
			crByDie = .5f - SkillsFactor.levelup(6, monster);
		} else if (andIntelligent("construct", monster)) {
			crByDie = .35f - SkillsFactor.levelup(2, monster);
		} else if (andIntelligent("undead", monster)) {
			crByDie = .35f - SkillsFactor.levelup(4, monster);
		} else if (type.contains("undead") || type.contains("construct")) {
			crByDie = .35f;
		} else if (andIntelligent("ooze", monster)) {
			crByDie = .55f - SkillsFactor.levelup(2, monster);
		} else if (type.contains("ooze") || type.contains("plant")
				|| type.contains("vermin")) {
			crByDie = .45f;
		} else {
			throw new RuntimeException("Unknown type " + type);
		}
		// crByDie -= Monster.getbonus(monster.intelligence) *
		// SkillsFactor.COST;
		return new Double(monster.originalhd * crByDie).floatValue();
	}

	private boolean andIntelligent(final String type, final Monster monster) {
		return monster.type.contains(type) && monster.intelligence != 0;
	}
}