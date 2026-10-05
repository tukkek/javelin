package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.precious.ArtPiece;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.Images;

/**
 * @see ArtPiece
 * @author alex
 */
public class ArtDisplay extends Chest{
	/** Constructor. */
	public ArtDisplay(Integer pool,DungeonFloor f){
		super(pool,f);
	}

	@Override
	protected boolean allow(Item i){
		return i.is(ArtPiece.class)!=null;
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","artdisplay"));
	}
}
