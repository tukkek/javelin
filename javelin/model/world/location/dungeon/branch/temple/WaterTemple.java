package javelin.model.world.location.dungeon.branch.temple;

import java.util.List;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Water;
import javelin.model.Realm;
import javelin.model.unit.Monster;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.feature.Fountain;

/**
 * Found next to {@link Water}. {@link Fight#weather} Always
 * {@link Weather#STORM}.
 *
 * It is placed on any {@link Terrain#NONWATER} tile next to water but
 * {@link Monster} pool is exclusively aquatic.
 *
 * TODO would be pretty cool to have "breath underwater" shrines that only take
 * you a certain number of tiles forward, forcing you to go back to shrines and
 * think strategically during exploration, with each level being flooded
 * according to their depth (meaning a 3/4-level water temple would be quite
 * tricky to finish). Swim skill/movement speed could factor into it, as well as
 * having the appropriate spell/scrolls.
 *
 * @author alex
 */
public class WaterTemple extends Temple{
	private static final String FLUFF="As you march towards the coastal construction you marvel at the sight of the waves crashing below you.\n"
			+"You recall a bard's song telling about how one day the entire earth would be swallowed by the rising oceans.\n"
			+"The air is moist and salty. You watch the motion of the nearby body of water as it dances back and forth patiently.\n"
			+"You hear a distant sound, unsure if it was a gull's cry, the wind hitting the wall besides you or the invitation of a hidden mermaid.";

	/** @see Location */
	public static class WaterTempleEntrance extends TempleEntrance{
		/** Constructor. */
		public WaterTempleEntrance(Temple t){
			super(t);
		}

		@Override
		protected boolean validatelocation(boolean water,World w,
				List<Actor> actors){
			return Terrain.search(new Point(x,y),Terrain.WATER,1,w)>0
					&&super.validatelocation(water,w,actors);
		}

		@Override
		protected void generate(boolean water){
			var t=dungeon.terrains;
			t.clear();
			t.addAll(Terrain.NONWATER);
			super.generate(false);
			t.clear();
			t.add(Terrain.get(x,y));
			t.add(Terrain.WATER);
		}
	}

	static class WaterBranch extends TempleBranch{
		protected WaterBranch(){
			super(Realm.WATER,"floordungeon","walltemplewater");
			doorbackground=false;
			features.add(Fountain.class);
		}

		@Override
		public void fight(Fight f){
			f.weather=Weather.STORM;
		}
	}

	/** Constructor. */
	public WaterTemple(){
		super(new WaterBranch(),FLUFF);
	}

	@Override
	protected void place(){
		new WaterTempleEntrance(this).place();
	}
}
