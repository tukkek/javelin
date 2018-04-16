package javelin.model.world.location.dungeon.temple;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.terrain.Hill;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.relic.Amulet;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.temple.features.Portal;

/**
 * Found atop a {@link Hill}. 1 portal per level takes you immediately outside.
 * All types of monsters can be found here.
 * 
 * @see Temple
 * @author alex
 */
public class MagicTemple extends Temple {
	private static final String FLUFF = "The air inside the castle feels laden with static.\n"
			+ "As you descend through the unsealed magic barrier your eyes begin to adjust to the light.\n"
			+ "There are a few stones along the walls, they give off a faint octarine glow.\n"
			+ "You walk along a grand mural written in an ancient language but alas you cannot decipher it.";

	/**
	 * @param level
	 *            Level of this temple.
	 * @see ChallengeCalculator#leveltoel(int)
	 */
	public MagicTemple(Integer level) {
		super(Realm.MAGIC, level, new Amulet(), FLUFF);
		terrain = Terrain.HILL;
		floor = "terrainwoodfloor";
		wall = "terrainwall";
	}

	@Override
	public ArrayList<Terrain> getterrains() {
		ArrayList<Terrain> terrains = new ArrayList<Terrain>();
		for (Terrain t : Terrain.NONUNDERGROUND) {
			terrains.add(t);
		}
		terrains.add(Terrain.UNDERGROUND);
		return terrains;
	}

	@Override
	public List<Feature> getfeatures(Dungeon d) {
		ArrayList<Feature> features = new ArrayList<Feature>();
		Point spot = d.findspot();
		features.add(new Portal(spot.x, spot.y));
		return features;
	}
}
