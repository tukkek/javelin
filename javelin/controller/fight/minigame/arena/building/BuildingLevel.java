package javelin.controller.fight.minigame.arena.building;

import javelin.controller.fight.minigame.arena.ArenaFight;

public class BuildingLevel{
	int level;
	int repair;
	int hp;
	int damagethresold;
	int hardness;
	int cost;
	static BuildingLevel[] LEVELS=new BuildingLevel[]{
	new BuildingLevel(0,5,70,60,5,0),
	new BuildingLevel(1,10,110,90,7,7500*ArenaFight.BOOST),
	new BuildingLevel(2,15,240,180,8,25000*ArenaFight.BOOST),
	new BuildingLevel(3,20,600,540,8,60000*ArenaFight.BOOST),};

	public BuildingLevel(int level,int repair,int hp,int damagethresold,
			int hardness,float cost){
		super();
		this.level=level;
		this.repair=repair;
		this.hp=hp;
		this.damagethresold=damagethresold;
		this.hardness=hardness;
		this.cost=Math.round(cost);
	}
}