/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;

/**
 * TODO subtypes arent being taken into consideration
 * 
 * Can't simply upgrade HD since it gives more base attack bonus, abilities...
 * (feats are taken into consideration by FeatsFactor)
 * 
 * @author alex
 * 
 */
public class HdFactor extends CrFactor {
	@Override
	public float calculate(final Monster monster) {
		final String type = monster.monsterType.toLowerCase();
		final float crByDie;
		if (type.contains("dragon")) {
			crByDie = .75f;
		} else if (type.contains("outsider")) {
			crByDie = .7f;
		} else if (type.contains("magical beast") || type.contains("beast")) {
			/* beast is a typo */
			crByDie = .65f;
		} else if (type.contains("mounstrous humanoid")) {
			crByDie = .6f;
		} else if (type.contains("aberration") || type.contains("animal")
				|| type.contains("elemental") || type.contains("giant")
				|| type.contains("humanoid") || andIntelligent("ooze", monster)
				|| andIntelligent("plant", monster)
				|| andIntelligent("vermin", monster)) {
			crByDie = .55f;
		} else if (type.contains("fey")) {
			crByDie = .5f;
		} else if (andIntelligent("construct", monster)
				|| type.contains("ooze") || type.contains("plant")
				|| andIntelligent("undead", monster) || type.contains("vermin")) {
			crByDie = .45f;
		} else if (type.contains("construct") || type.contains("undead")) {
			crByDie = .35f;
		} else {
			throw new RuntimeException("Unknown type " + type);
		}
		return new Double(monster.originalhd * crByDie).floatValue();
	}

	private boolean andIntelligent(final String type, final Monster monster) {
		return monster.monsterType.contains(type) && monster.intelligence != 0;
	}
}