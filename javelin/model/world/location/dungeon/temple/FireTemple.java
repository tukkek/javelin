package javelin.model.world.location.dungeon.temple;

import javelin.controller.terrain.Desert;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.artifact.Candle;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.feature.Brazier;

/**
 * Found buried in the {@link Desert}. 1-3 pedestals light an area around the
 * cave.
 *
 * @see Temple
 * @author alex
 */
public class FireTemple extends Temple{
	private static final String FLUFF="This mighty construction isn't like anything you've ever seen in the desert.\n"
			+"It stands tall among the dunes and somehow seems even older than they are, yet abandoned for maybe just as long.\n"
			+"This was clearly built as a strong defensive outpost - the walls are tall and strong, the towers full of small openings for defense.\n"
			+"As you creep in you can't help but wonder who have built these halls, why'd they leave it and where could they be now.";

	/** Constructor. */
	public FireTemple(Integer level){
		super(Realm.FIRE,Terrain.DESERT,level,new Candle(level),FLUFF);
		images.put(DungeonImages.FLOOR,"floortemplefire");
		images.put(DungeonImages.WALL,"walltemplefire");
		doorbackground=false;
		feature=Brazier.class;
	}
}
