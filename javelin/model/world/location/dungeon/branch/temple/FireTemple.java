package javelin.model.world.location.dungeon.branch.temple;

import javelin.controller.content.terrain.Desert;
import javelin.controller.content.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.feature.Brazier;

/**
 * Found buried in the {@link Desert}. 1-3 pedestals light an area around the
 * cave.
 *
 * @see Temple
 * @author alex
 */
public class FireTemple extends Temple{
	/** Branch singleton. */
	public static final Branch BRANCH=new FireBranch();

	static final String FLUFF="This mighty construction isn't like anything else you've seen in the desert.\n"
			+"It stands tall among the dunes and somehow seems even older than they are, yet abandoned for maybe almost as long.\n"
			+"This was clearly built as a strong defensive outpost - the walls are tall and strong, the towers full of small openings for defense.\n"
			+"As you creep in you can't help but wonder who built such a structure, why'd they leave it and where could they be now.";

	static class FireBranch extends Branch{
		FireBranch(){
			super("Burning","of fire","floortemplefire","walltemplefire");
			features.add(Brazier.class);
			terrains.add(Terrain.DESERT);
			doorbackground=false;
		}
	}

	/** Constructor. */
	public FireTemple(){
		super(Realm.FIRE,new FireBranch(),FLUFF);
	}
}
