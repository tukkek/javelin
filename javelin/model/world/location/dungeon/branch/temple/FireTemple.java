package javelin.model.world.location.dungeon.branch.temple;

import javelin.controller.terrain.Desert;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.world.location.dungeon.feature.Brazier;

/**
 * Found buried in the {@link Desert}. 1-3 pedestals light an area around the
 * cave.
 *
 * @see Temple
 * @author alex
 */
public class FireTemple extends Temple{
	static final String FLUFF="This mighty construction isn't like anything you've ever seen in the desert.\n"
			+"It stands tall among the dunes and somehow seems even older than they are, yet abandoned for maybe just as long.\n"
			+"This was clearly built as a strong defensive outpost - the walls are tall and strong, the towers full of small openings for defense.\n"
			+"As you creep in you can't help but wonder who have built these halls, why'd they leave it and where could they be now.";

	public static class FireBranch extends TempleBranch{
		FireBranch(){
			super(Realm.FIRE,"floortemplefire","walltemplefire");
			features.add(Brazier.class);
			terrains.add(Terrain.DESERT);
			doorbackground=false;
		}
	}

	/** Constructor. */
	public FireTemple(){
		super(new FireBranch(),FLUFF);
	}
}
