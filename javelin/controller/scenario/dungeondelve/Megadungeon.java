package javelin.controller.scenario.dungeondelve;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.inhabitant.Trader;
import javelin.old.RPG;

/**
 * Individual floots that make up a single megadungeon for {@link DungeonDelve}.
 *
 * @see DungeonDelve#getdungeons()
 *
 * @author alex
 */
/**
 * @author alex
 *
 */
public class Megadungeon extends Dungeon{
	static final String DESCRIPTION="Megadungeon";
	/**
	 * Ideally once per tier (1/5) but that is cutting it too close and entire
	 * tiers would often end up without a single trader. Giving it a little boost
	 * seems to soft-guarantee at least one vendor per tier.
	 */
	static final int TRADERCHANCE=4;

	/** Constructor. */
	public Megadungeon(Integer level,Dungeon parent){
		super(DESCRIPTION,level,parent,DungeonDelve.getdungeons());
		description=DESCRIPTION;
	}

	@Override
	protected Feature createspecialchest(Point p){
		if(floors.indexOf(this)!=floors.size()-1)
			return super.createspecialchest(p);
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
		if(RPG.chancein(TRADERCHANCE)) zoner.place(new Trader());
	}
}
