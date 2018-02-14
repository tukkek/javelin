package javelin.model.world.location.dungeon.temple;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.terrain.Forest;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.relic.Map;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.temple.features.FruitTree;
import tyrant.mikera.engine.RPG;

/**
 * Found deep in the {@link Forest}. Apple trees restore health of 1 unit.
 * 
 * @see Temple
 * @author alex
 */
public class EarthTemple extends Temple {
	private static final String FLUFF = "After trekking the woods for hours you reach an area that looks rather unique.\n"
			+ "You're not sure whether the stronger coloration of the flora around you or the way the shadows seems to twist is making you more unnerved.\n"
			+ "A slimy little frog looks at you from atop a big boulder, unaware of you quest.\n"
			+ "As you try to find you way through the temple's entrance you must tear away the overgrowth as you wander in.";

	/** Constructor. */
	public EarthTemple(Integer pop) {
		super(Realm.EARTH, pop, new Map(), FLUFF);
		terrain = Terrain.FOREST;
		floor = "terraindirt";
		wall = "dungeonwalltempleearth";
	}

	@Override
	public List<Feature> getfeatures(Dungeon d) {
		int ntrees = RPG.r(1, 3);
		ArrayList<Feature> trees = new ArrayList<Feature>();
		for (int i = 0; i < ntrees; i++) {
			Point spot = d.findspot();
			trees.add(new FruitTree(spot.x, spot.y));
		}
		return trees;
	}
}
