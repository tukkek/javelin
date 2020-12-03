package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.Images;

/**
 * Usually one {@link DungeonFloor}.
 *
 * @see ArtifactChest
 * @author alex
 */
public class SpecialChest extends Chest{
	/** Constructor. */
	public SpecialChest(DungeonFloor f,Item i){
		super(i,f);
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","special"));
	}
}