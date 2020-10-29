package javelin.model.world.location.dungeon.temple;

import javelin.controller.terrain.Forest;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.artifact.Map;
import javelin.model.world.location.dungeon.feature.FruitTree;

/**
 * Found deep in the {@link Forest}. Apple trees restore health of 1 unit.
 *
 * @see Temple
 * @author alex
 */
public class EarthTemple extends Temple{
	private static final String FLUFF="After trekking the woods for hours you reach an area that looks rather unique.\n"
			+"You're not sure whether the stronger coloration of the flora around you or the way the shadows seems to twist is making you more unnerved.\n"
			+"A slimy little frog looks at you from atop a big boulder, unaware of you quest.\n"
			+"As you try to find you way through the temple's entrance you must tear away the overgrowth as you wander in.";

	/** Constructor. */
	public EarthTemple(Integer level){
		super(Realm.EARTH,level,new Map(level),FLUFF);
		terrain=Terrain.FOREST;
		floor="terraindirt";
		wall="dungeonwalltempleearth";
		doorbackground=false;
		feature=FruitTree.class;
	}
}
