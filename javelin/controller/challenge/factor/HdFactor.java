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
	public static class TypeData {
		/** Challenge rating per HD. */
		public float cr;
		/** How many skill points per HD. */
		public int skillprogression;

		/** Constructor. */
		public TypeData(float cr, int skillprogression) {
			this.cr = cr;
			this.skillprogression = skillprogression;
		}
	}

	@Override
	public float calculate(final Monster monster) {
		TypeData typedata = gettypedata(monster);
		float perhd = typedata.cr;
		if (typedata.skillprogression != 0) {
			perhd -= SkillsFactor.levelupcost(typedata.skillprogression,
					monster);
		}
		return monster.originalhd * perhd;
	}

	/**
	 * @return Information about this monster type.
	 */
	public static TypeData gettypedata(final Monster monster) {
		final String type = monster.type.toLowerCase();
		if (type.contains("dragon")) {
			return new TypeData(.75f, 6);
		}
		if (type.contains("outsider")) {
			return new TypeData(.7f, 8);
		}
		if (type.contains("magical beast") || type.contains("beast")
				|| type.contains("shapechanger")) {
			/* beast is a typo */
			return new TypeData(.65f, 2);
		}
		if (type.contains("monstrous humanoid")) {
			return new TypeData(.6f, 2);
		}
		if (type.contains("aberration") || type.contains("animal")
				|| type.contains("elemental") || type.contains("giant")
				|| type.contains("humanoid") || andIntelligent("plant", monster)
				|| andIntelligent("vermin", monster)) {
			return new TypeData(.55f, 2);
		}
		if (type.contains("fey")) {
			return new TypeData(.5f, 6);
		}
		if (andIntelligent("construct", monster)) {
			return new TypeData(.45f, 2);
		}
		if (andIntelligent("undead", monster)) {
			return new TypeData(.45f, 4);
		}
		if (type.contains("undead") || type.contains("construct")) {
			return new TypeData(.35f, 0);
		}
		if (andIntelligent("ooze", monster)) {
			return new TypeData(.55f, 2);
		}
		if (type.contains("ooze") || type.contains("plant")
				|| type.contains("vermin")) {
			return new TypeData(.45f, 6);
		}
		throw new RuntimeException("Unknown type " + type);
	}

	static boolean andIntelligent(final String type, final Monster monster) {
		return monster.type.contains(type) && monster.intelligence != 0;
	}
}