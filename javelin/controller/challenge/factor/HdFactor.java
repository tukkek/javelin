/**
 * Alex Henry on 13/10/2010
 */
package javelin.controller.challenge.factor;

import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * Can't simply upgrade HD since it gives more base attack bonus, abilities...
 * (feats are taken into consideration by FeatsFactor)
 *
 * @see CrFactor
 *
 * @author alex
 */
public class HdFactor extends CrFactor{
	public static class TypeData{
		/** Challenge rating per HD. */
		public float cr;
		/** How many skill points per HD. */
		public int skillprogression;

		/** Constructor. */
		public TypeData(float cr,int skillprogression){
			this.cr=cr;
			this.skillprogression=skillprogression;
		}
	}

	@Override
	public float calculate(final Monster monster){
		TypeData typedata=gettypedata(monster);
		float perhd=typedata.cr;
		if(typedata.skillprogression!=0)
			perhd-=SkillsFactor.levelupcost(typedata.skillprogression,monster);
		return monster.originalhd*perhd;
	}

	/**
	 * @return Information about this monster type.
	 */
	public static TypeData gettypedata(final Monster monster){
		final MonsterType type=monster.type;
		if(type==MonsterType.DRAGON) return new TypeData(.75f,6);
		if(type==MonsterType.OUTSIDER) return new TypeData(.7f,8);
		if(type==MonsterType.MAGICAL_BEAST||type==MonsterType.SHAPECHANGER)
			return new TypeData(.65f,2);
		if(type==MonsterType.MONSTROUS_HUMANOID) return new TypeData(.6f,2);
		if(type==MonsterType.ABERRATION||type==MonsterType.ANIMAL
				||type==MonsterType.ELEMENTAL||type==MonsterType.GIANT
				||type==MonsterType.HUMANOID||andIntelligent(MonsterType.PLANT,monster)
				||andIntelligent(MonsterType.VERMIN,monster))
			return new TypeData(.55f,2);
		if(type==MonsterType.FEY) return new TypeData(.5f,6);
		if(andIntelligent(MonsterType.CONSTRUCT,monster))
			return new TypeData(.45f,2);
		if(andIntelligent(MonsterType.UNDEAD,monster)) return new TypeData(.45f,4);
		if(type==MonsterType.UNDEAD||type==MonsterType.CONSTRUCT)
			return new TypeData(.35f,0);
		if(andIntelligent(MonsterType.OOZE,monster)) return new TypeData(.55f,2);
		if(type==MonsterType.OOZE||type==MonsterType.PLANT
				||type==MonsterType.VERMIN)
			return new TypeData(.45f,6);
		throw new RuntimeException("Unknown type "+type);
	}

	static boolean andIntelligent(final MonsterType type,final Monster monster){
		return monster.type==type&&monster.intelligence!=0;
	}
}