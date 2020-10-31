package javelin.model.world.location.dungeon.temple;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Water;
import javelin.model.Realm;
import javelin.model.item.artifact.Crown;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Fountain;

/**
 * Found next to {@link Water}. Always flooded.
 *
 * TODO would be pretty cool to have "breath underwater" shrines that only take
 * you a certain number of tiles forward, forcing you to go back to shrines and
 * think strategically during exploration, with each level being flooded
 * according to their depth (meaning a 3/4-level water temple would be quite
 * tricky to finish). Swim skill/movement speed could factor into it, as well as
 * having the appropriate spell/scrolls.
 *
 * @see Temple
 * @see Fight#weather
 * @author alex
 */
public class WaterTemple extends Temple{
	private static final String FLUFF="As you march towards the coastal construction you marvel at the sight of the waves crashing below you.\n"
			+"You recall a bard's song telling about how one day the entire earth would be swallowed by the rising oceans.\n"
			+"The air is moist and salty. You watch the motion of the nearby body of water as it dances back and forth patiently.\n"
			+"You hear a distant sound, unsure if it was a gull's cry, the wind hitting the wall besides you or the invitation of a hidden mermaid.";

	/** Constructor. */
	public WaterTemple(Integer level){
		super(Realm.WATER,level,new Crown(level),FLUFF);
		terrain=Terrain.WATER;
		floor="floordungeon";
		wall="walltemplewater";
		doorbackground=false;
		feature=Fountain.class;
	}

	@Override
	public Fight encounter(Dungeon d){
		Fight f=super.encounter(d);
		f.weather=Weather.STORM;
		return f;
	}

	@Override
	protected void generate(){
		while(x==-1
				||Terrain.search(new Point(x,y),Terrain.WATER,1,World.getseed())==0)
			super.generate();
	}
}
