package javelin.model.world.location.dungeon.branch.temple;

import javelin.controller.terrain.Forest;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.FruitTree;

/**
 * Found deep in the {@link Forest}. Apple trees restore health of 1 unit.
 *
 * @see Temple
 * @author alex
 */
public class EarthTemple extends Temple{
	/** Branch singleton. */
	public static final Branch BRANCH=new EarthBranch();

	static final String FLUFF="After trekking the woods for hours you reach an area that looks rather unique.\n"
			+"You're not sure whether the stronger coloration of the flora around you or the way the shadows seems to twist is making you more unnerved.\n"
			+"A slimy little frog looks at you from atop a big boulder, unaware of your quest.\n"
			+"As you try to find you way through the temple's entrance you must tear away the overgrowth as you wander in.";

	static class EarthBranch extends Branch{
		/** Constructor. */
		EarthBranch(){
			super("Stone","of earth","floordirt","walltempleearth");
			features.add(FruitTree.class);
			terrains.add(Terrain.FOREST);
		}
	}

	/** Constructor. */
	public EarthTemple(){
		super(Realm.EARTH,new EarthBranch(),FLUFF);
	}
}
