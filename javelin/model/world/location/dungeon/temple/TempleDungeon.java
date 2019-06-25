package javelin.model.world.location.dungeon.temple;

import java.util.List;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.controller.terrain.Terrain;
import javelin.model.item.Ruby;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.Altar;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.old.RPG;

/**
 * Unlike normal dungeons {@link Temple}s have many floors (levels), chests with
 * rubies and an Altar on the deepest level.
 *
 * @author alex
 */
public class TempleDungeon extends Dungeon{
	public Temple temple;

	/**
	 * @param level Encounter level.
	 * @param deepest <code>true</code> if the last (bottom) floor.
	 * @param parent Previous dungeon level.
	 * @param t Temple this floor is a part of.
	 */
	public TempleDungeon(int level,Dungeon parent,Temple t){
		super(t.description,level,parent,t.floors);
		temple=t;
		description=temple.descriptionknown;
	}

	@Override
	protected void registerinstance(){
		// don't
	}

	@Override
	protected void deregisterinstance(){
		// don't
	}

	@Override
	public void activate(boolean loading){
		doorbackground=temple.doorbackground;
		if(temple.floor!=null) floortile=temple.floor;
		if(temple.wall!=null) walltile=temple.wall;
		if(loading||Dungeon.active!=null){
			super.activate(loading);
			return;
		}
		String difficulty=Difficulty.describe(
				temple.el-ChallengeCalculator.calculateel(Squad.active.members));
		Character prompt=Javelin
				.prompt("You're at the entrance of the "+temple.descriptionknown
						+" (difficulty: "+difficulty+"). Do you want to enter?\n"
						+"Press ENTER to venture forth or any other key to cancel...");
		if(prompt.equals('\n')) super.activate(loading);
	}

	@Override
	protected Feature createspecialchest(Point p){
		if(floors.indexOf(this)==floors.size()-1) return new Altar(p,temple);
		Chest c=new Chest(p.x,p.y);
		c.items.add(new Ruby());
		c.setspecial();
		return c;
	}

	@Override
	protected void createfeatures(int nfeatures,DungeonZoner zoner){
		if(this==temple.floors.get(0)){
			CommonFeatureTable table=tables.get(CommonFeatureTable.class);
			int size=table.getchances();
			table.add(Fountain.class,size);
			if(temple.feature!=null) table.add(temple.feature,size);
		}
		super.createfeatures(nfeatures,zoner);
	}

	@Override
	public Fight encounter(){
		return temple.encounter(this);
	}

	@Override
	public boolean hazard(){
		return temple.hazard(this);
	}

	@Override
	protected boolean expire(){
		/* Temples expiring is handled by the Altar feature */
		return false;
	}

	@Override
	protected Combatants generateencounter(int level,List<Terrain> terrains)
			throws GaveUp{
		terrains=temple.getterrains();
		Combatants encounter=super.generateencounter(level-RPG.r(1,4),terrains);
		if(!temple.validate(encounter.getmonsters())) return null;
		while(ChallengeCalculator.calculateel(encounter)<level)
			encounter.getweakest().upgrade();
		return encounter;
	}

	@Override
	protected String baptize(DungeonTier tier){
		return null;
	}
}
