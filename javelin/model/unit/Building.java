package javelin.model.unit;

import javelin.Javelin;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.quality.resistance.CriticalImmunity;
import javelin.controller.quality.resistance.MindImmunity;
import javelin.controller.quality.resistance.ParalysisImmunity;
import javelin.controller.quality.resistance.PoisonImmunity;
import javelin.model.state.BattleState;

public class Building extends Combatant{
	public static class BuildingLevel{
		public int level;
		public int repair;
		public int hp;
		public int damagethresold;
		public int hardness;
		public int cost;

		public BuildingLevel(int level,int repair,int hp,int damagethresold,
				int hardness,float cost){
			super();
			this.level=level;
			this.repair=repair;
			this.repair=level; //TODO
			this.hp=hp;
			this.damagethresold=damagethresold;
			this.hardness=hardness;
			this.cost=Math.round(cost);
		}
	}

	public static final float CRADJUSTMENT=CriticalImmunity.CR+MindImmunity.CR
			+ParalysisImmunity.CR+PoisonImmunity.CR;

	public static BuildingLevel[] LEVELS=new BuildingLevel[]{
			new BuildingLevel(0,5,70,60,5,0),
			new BuildingLevel(1,10,110,90,7,7500*ArenaFight.BOOST),
			new BuildingLevel(2,15,240,180,8,25000*ArenaFight.BOOST),
			new BuildingLevel(3,20,600,540,8,60000*ArenaFight.BOOST),};

	static final Monster MONSTER=Javelin.getmonster("building");

	/** Building level from 0 to 4. */
	public int level=0;

	public Building(String name,String avatar){
		super(MONSTER,false);
		source.customName=name;
		source.avatarfile=avatar;
		source.passive=true;
		source.immunitytocritical=true;
		source.immunitytomind=true;
		source.immunitytoparalysis=true;
		source.immunitytopoison=true;
		setlevel(Building.LEVELS[0]);
	}

	@Override
	public String getstatus(){
		switch(getnumericstatus()){
			case STATUSUNHARMED:
				return "pristine";
			case STATUSSCRATCHED:
				return "scathed";
			case STATUSHURT:
				return "worn";
			case STATUSWOUNDED:
				return "broken";
			case STATUSINJURED:
				return "torn";
			case STATUSDYING:
				return "demolished";
			case STATUSUNCONSCIOUS:
			case STATUSDEAD:
				return "destroyed";
			default:
				throw new RuntimeException("Unknown possibility: "+getnumericstatus());
		}
	}

	public void setlevel(BuildingLevel level){
		this.level=level.level;
		maxhp=level.hp;
		hp=maxhp;
		source.dr=level.hardness;
		source.cr=(level.level+1)*5f;
	}

	@Override
	public void act(BattleState s){
		ap+=1;
	}

}