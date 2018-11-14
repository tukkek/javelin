package javelin.controller.scenario.dungeondelve;

import java.util.List;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.table.dungeon.Trader;
import javelin.model.item.Ruby;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.StairsDown;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;
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
	 * {@link District}s to train and spend money on, these features are
	 * artifically boosted on the {@link Megadungeon}. Note that {@link Chest}s
	 * here will be turned into special chests with a Wish {@link Ruby} inside, to
	 * make up for the aggravated lack of balancing for {@link Dungeon}-only play
	 * in Javelin.
	 *
	 * TODO add merchant {@link Inhabitant}, learning stones
	 */
	public static final List<Class<? extends Feature>> FEATURES=List
			.of(Fountain.class,Chest.class,Trader.class);
	/**
	 * The fraction of {@link Dungeon} {@link Feature}s that will be normal,
	 * compared to the ones provided by {@link #FEATURES}.
	 */
	public static final int NORMALFEATURES=3;

	public Megadungeon(Integer level,Dungeon parent){
		super(level,parent);
	}

	@Override
	protected void createstairs(Point p){
		super.createstairs(p);
		if(level!=DungeonDelve.LEVELS) features.add(new StairsDown(findspot()));
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
		if(!DungeonDelve.hasmcguffin()) return super.encounter();
		return DungeonDelve.getdungeons().get(DungeonDelve.LEVELS).encounter();
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
