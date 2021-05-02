package javelin.model.world.location.dungeon.branch.temple;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.content.terrain.Hill;
import javelin.controller.content.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.location.dungeon.Portal;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.rare.Throne;

/**
 * Found atop a {@link Hill}.
 *
 * TODO would be pretty cool if the magic temple had all doors replaced with
 * walls and only {@link Portal}s could be used for exploration between rooms.
 *
 * @author alex
 */
public class MagicTemple extends Temple{
	/** Branch singleton. */
	public static final Branch BRANCH=new MagicBranch();

	static final String FLUFF="The air inside the castle feels laden with static.\n"
			+"As you descend through the unsealed magic barrier your eyes begin to adjust to the light.\n"
			+"There are a few stones along the entry walls and they give off a faint octarine glow.\n"
			+"You walk along a grand mural written in an ancient language but alas you cannot decipher it.";

	static class MagicBranch extends Branch{
		protected MagicBranch(){
			super("Enchanted","of magic","floortemplemagic","walltemplemagic");
			features.add(Throne.class);
			terrains.add(Terrain.HILL);
		}
	}

	/**
	 * @param level Level of this temple.
	 * @see ChallengeCalculator#leveltoel(int)
	 */
	public MagicTemple(){
		super(Realm.MAGIC,new MagicBranch(),FLUFF);
	}
}
