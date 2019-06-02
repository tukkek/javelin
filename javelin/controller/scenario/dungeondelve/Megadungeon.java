package javelin.controller.scenario.dungeondelve;

import javelin.controller.Point;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.model.item.Ruby;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonZoner;
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
	//	public static final List<Class<? extends Feature>> FEATURES=List
	//			.of(Fountain.class,LearningStone.class,LearningStone.class
	//			//					,Chest.class
	//			//			Campfire.class,Trader.class
	//			);

	int floor;

	public Megadungeon(int floor,Integer level,Dungeon parent){
		super(level,parent);
		this.floor=floor;
		description="Mega-dungeon, floor "+floor;
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
		//		var special=RPG.r(0,nfeatures);
		//		nfeatures-=special;
		super.createfeatures(nfeatures,zoner);
		for(int i=1+RPG.randomize(2);i>=0;i++)
			zoner.place(new Fountain());
		for(int i=1+RPG.randomize(2);i>=0;i++)
			zoner.place(new LearningStone());
		//		try{
		//			for(;special>0;special--){
		//				Class<? extends Feature> type=RPG.pick(FEATURES);
		//				if(Chest.class.equals(type)){
		//					var c=new Chest(-1,-1,new Ruby());
		//					c.setspecial();
		//					zoner.place(c);
		//				}else
		//					zoner.place(type.getConstructor().newInstance());
		//			}
		//		}catch(ReflectiveOperationException e){
		//			throw new RuntimeException(e);
		//		}
	}
}
