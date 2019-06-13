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
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.dungeon.feature.StairsUp;
import javelin.old.RPG;

/**
 * Unlike normal dungeons {@link Temple}s have many floors (levels), chests with
 * rubies and an Altar on the deepest level.
 *
 * @author alex
 */
public class TempleDungeon extends Dungeon{
	public Temple temple;
	/** <code>true</code> if last dungeon level. */
	public boolean deepest;

	/**
	 * @param level Encounter level.
	 * @param deepest <code>true</code> if the last (bottom) floor.
	 * @param parent Previous dungeon level.
	 * @param t Temple this floor is a part of.
	 */
	public TempleDungeon(int level,boolean deepest,Dungeon parent,Temple t){
		super(level,parent);
		temple=t;
		this.deepest=deepest;
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
	protected void setlocation(boolean loading){
		if(!loading) if(Temple.leavingfight)
			Temple.leavingfight=false;
		else{
			Class<? extends Feature> stairs;
			stairs=Temple.climbing?StairsDown.class:StairsUp.class;
			Temple.climbing=false;
			for(Feature f:features)
				if(stairs.isInstance(f)){
					herolocation.x=f.x;
					herolocation.y=f.y;
					break;
				}
		}
		super.setlocation(loading);
	}

	@Override
	protected Feature createspecialchest(Point p){
		if(deepest) return new Altar(p,temple);
		Chest c=new Chest(p.x,p.y);
		c.items.add(new Ruby());
		c.setspecial();
		return c;
	}

	@Override
	public void goup(){
		int level=temple.floors.indexOf(this);
		if(level==0)
			super.goup();
		else{
			Squad.active.ellapse(1);
			Temple.climbing=true;
			temple.floors.get(level-1).activate(false);
		}
	}

	@Override
	public void godown(){
		Squad.active.ellapse(1);
		temple.floors.get(temple.floors.indexOf(this)+1).activate(false);
	}

	@Override
	protected void createstairs(DungeonZoner zoner){
		super.createstairs(zoner);
		if(!deepest) features.add(new StairsDown(zoner.getpoint()));
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
