package javelin.model.world.location.dungeon.temple;

import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.terrain.Hill;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.artifact.Amulet;
import javelin.model.world.location.dungeon.feature.Portal;

/**
 * Found atop a {@link Hill}. 1 portal per level takes you immediately outside.
 * All types of monsters can be found here.
 *
 * TODO would be pretty cool if the magic temple had all doors replaced with
 * walls and only {@link Portal}s could be used for exploration between rooms.
 *
 * @author alex
 */
public class MagicTemple extends Temple{
	private static final String FLUFF="The air inside the castle feels laden with static.\n"
			+"As you descend through the unsealed magic barrier your eyes begin to adjust to the light.\n"
			+"There are a few stones along the walls, they give off a faint octarine glow.\n"
			+"You walk along a grand mural written in an ancient language but alas you cannot decipher it.";

	/**
	 * @param level Level of this temple.
	 * @see ChallengeCalculator#leveltoel(int)
	 */
	public MagicTemple(Integer level){
		super(Realm.MAGIC,level,new Amulet(level),FLUFF);
		terrain=Terrain.HILL;
		floor="terrainwoodfloor";
		wall="terrainwall";
		feature=Portal.class;
	}

	@Override
	public List<Terrain> getterrains(){
		return Terrain.NONWATER;
	}
}
