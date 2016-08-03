package javelin.model.world.location.dungeon.temple;

import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.Water;
import javelin.model.Realm;
import javelin.model.item.relic.Crown;
import javelin.model.world.World;

/**
 * Found next to {@link Water}. Always flooded.
 * 
 * @see Temple
 * @see Fight#weather
 * @author alex
 */
public class WaterTemple extends Temple {
	private static final String FLUFF =
			"As you march towards the coastal construction you marvel at the sight of the waves crashing below you.\n"
					+ "You recall a bard's song telling about how one day the entire earth would be swallowed by the rising oceans.\n"
					+ "The air is moist and salty. You watch the motion of the nearby body of water as it dances back and forth patiently.\n"
					+ "You hear a distant sound, unsure if it was a gull's cry, the wind hitting the wall besides you or the invitation of a hidden mermaid.";

	/** Constructor. */
	public WaterTemple(Integer pop) {
		super(Realm.WATER, pop, new Crown(), FLUFF);
		terrain = Terrain.WATER;
		floor = "terraindungeonfloor";
		wall = "dungeonwalltemplewater";
	}

	@Override
	public Fight encounter() {
		Fight f = super.encounter();
		f.weather = Weather.STORM;
		return f;
	}

	@Override
	protected void generate() {
		while (x == -1 || Terrain.checkadjacent(new Point(x, y), Terrain.WATER,
				World.seed, 1) == 0) {
			super.generate();
		}
	}
}
