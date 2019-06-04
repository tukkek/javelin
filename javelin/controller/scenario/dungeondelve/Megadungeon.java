package javelin.controller.scenario.dungeondelve;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.old.RPG;

/**
 * Individual floots that make up a single megadungeon for {@link DungeonDelve}.
 *
 * @see DungeonDelve#getdungeons()
 *
 * @author alex
 */
public class Megadungeon extends Dungeon{
	public Megadungeon(Integer level,Dungeon parent){
		super(level,parent);
		description="Megadungeon";
	}

	@Override
	protected void createstairs(DungeonZoner zoner){
		super.createstairs(zoner);
		if(floor!=DungeonDelve.FLOORS)
			features.add(new StairsDown(zoner.getpoint()));
	}

	@Override
	public void goup(){
		if(floor==1){
			super.goup();
			return;
		}
		Squad.active.ellapse(1);
		DungeonDelve.getdungeons().get(floor-1).activate(false);
	}

	@Override
	public void godown(){
		Squad.active.ellapse(1);
		DungeonDelve.getdungeons().get(floor+1).activate(false);
	}

	@Override
	protected Feature createspecialchest(Point p){
		if(floor!=DungeonDelve.FLOORS) return super.createspecialchest(p);
		var c=new Chest(p.x,p.y,new McGuffin());
		c.setspecial();
		return c;
	}

	@Override
	public Fight encounter(){
		if(DungeonDelve.get().climbmode){
			var deepest=DungeonDelve.getdungeons().get(DungeonDelve.FLOORS);
			return new RandomDungeonEncounter(deepest);
		}
		return super.encounter();
	}

	@Override
	protected void createfeatures(int nfeatures,DungeonZoner zoner){
		super.createfeatures(nfeatures,zoner);
		for(int i=1+RPG.randomize(2);i>0;i--)
			zoner.place(new Fountain());
		for(int i=1+RPG.randomize(2);i>0;i--)
			zoner.place(new LearningStone());
	}
}
