package javelin.model.world.location.unique;

import javelin.model.item.Tier;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.DungeonTier;

/**
 * A {@link DungeonFloor} from {@link Tier#LOW} to {@link Tier#HIGH}, mainly for
 * roguelike-oriented players (or play sessions) who just want to dungeon crawl
 * and not bother with anything else.
 *
 * TODO should probably offer something cooler at the end of it? Maybe make it
 * one of the ways to win the game?
 *
 * @author alex
 */
public class DeepDungeon extends Dungeon{
	static final String DESCRIPTION="Deep dungeon";

	/** @see Location */
	public static class DeepDungeonEntrance extends DungeonEntrance{
		/** Constructor. */
		public DeepDungeonEntrance(DeepDungeon d){
			super(d);
		}

		@Override
		protected void generate(boolean water){
			//handled by LocationGenerator
		}
	}

	class DeepDungeonFloor extends DungeonFloor{
		DeepDungeonFloor(Integer level,Dungeon d){
			super(level,d);
		}

		@Override
		public DungeonTier gettier(){
			return DungeonTier.get(level);
		}
	}

	/** Constructor. */
	public DeepDungeon(){
		super(DESCRIPTION,Tier.LOW.minlevel,Tier.EPIC.maxlevel);
		images=new DungeonImages(DungeonTier.TEMPLE);
	}

	@Override
	public String getimagename(){
		return "deepdungeon";
	}

	@Override
	protected DungeonFloor createfloor(int level){
		return new DeepDungeonFloor(level,this);
	}

	@Override
	protected synchronized String baptize(String suffix){
		return name;
	}
}
