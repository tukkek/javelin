package javelin.controller.scenario.dungeondelve;

import java.util.List;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.table.dungeon.Trader;
import javelin.model.item.Ruby;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.Campfire;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.town.District;
import javelin.old.RPG;

/**
 * Individual floots that make up a single megadungeon for {@link DungeonDelve}.
 *
 * @see DungeonDelve#getdungeons()
 *
 * @author alex
 */
public class Megadungeon extends Dungeon{
	/**
	 * Since {@link DungeonDelve} characters cannot rely on usual
	 * {@link District}s to train, spend money on and heal, these features are
	 * artifically boosted on the {@link Megadungeon}. Note that {@link Chest}s
	 * here will be turned into special chests with a Wish {@link Ruby} inside, to
	 * make up for the aggravated lack of balancing for {@link Dungeon}-only play
	 * in Javelin.
	 *
	 * Roughly half of it should be resting/healing features since that's the most
	 * important for basic survival.
	 */
	public static final List<Class<? extends Feature>> FEATURES=List.of(
			Fountain.class,Campfire.class,LearningStone.class,Chest.class,
			Trader.class);
	/**
	 * The fraction of {@link Dungeon} {@link Feature}s that will be normal,
	 * compared to the ones provided by {@link #FEATURES}.
	 */
	public static final int NORMALFEATURES=3;

	public Megadungeon(Integer level,Dungeon parent){
		super(level,parent);
	}

	@Override
	protected void createstairs(DungeonZoner zoner){
		super.createstairs(zoner);
		if(level!=DungeonDelve.LEVELS)
			features.add(new StairsDown(zoner.getpoint()));
	}

	@Override
	public void goup(){
		if(level==1){
			super.goup();
			return;
		}
		Squad.active.ellapse(1);
		DungeonDelve.getdungeons().get(level-1).activate(false);
	}

	@Override
	public void godown(){
		Squad.active.ellapse(1);
		DungeonDelve.getdungeons().get(level+1).activate(false);
	}

	@Override
	protected Feature createspecialchest(Point p){
		if(level!=DungeonDelve.LEVELS) return super.createspecialchest(p);
		var c=new Chest(p.x,p.y,new McGuffin());
		c.setspecial();
		return c;
	}

	@Override
	public Fight encounter(){
		if(DungeonDelve.get().climbmode){
			var deepest=DungeonDelve.getdungeons().get(DungeonDelve.LEVELS);
			return new RandomDungeonEncounter(deepest);
		}
		return super.encounter();
	}

	@Override
	protected Feature createfeature(){
		if(RPG.chancein(NORMALFEATURES)) return super.createfeature();
		try{
			Class<? extends Feature> type=RPG.pick(FEATURES);
			if(Chest.class.equals(type)){
				var c=new Chest(-1,-1,new Ruby());
				c.setspecial();
				return c;
			}
			return type.getConstructor().newInstance();
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}
}
