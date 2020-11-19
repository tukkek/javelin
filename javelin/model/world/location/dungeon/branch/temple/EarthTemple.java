package javelin.model.world.location.dungeon.branch.temple;

import javelin.controller.terrain.Forest;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.location.dungeon.feature.FruitTree;

/**
 * Found deep in the {@link Forest}. Apple trees restore health of 1 unit.
 *
 * @see Temple
 * @author alex
 */
public class EarthTemple extends Temple{
	static final String FLUFF="After trekking the woods for hours you reach an area that looks rather unique.\n"
			+"You're not sure whether the stronger coloration of the flora around you or the way the shadows seems to twist is making you more unnerved.\n"
			+"A slimy little frog looks at you from atop a big boulder, unaware of you quest.\n"
			+"As you try to find you way through the temple's entrance you must tear away the overgrowth as you wander in.";

	public static class EarthBranch extends TempleBranch{
		public EarthBranch(){
			super(Realm.EARTH,"floordirt","walltempleearth");
			features.add(FruitTree.class);
			terrains.add(Terrain.FOREST);
		}
	}

	/** Constructor. */
	public EarthTemple(){
		super(new EarthBranch(),FLUFF);
	}
}
