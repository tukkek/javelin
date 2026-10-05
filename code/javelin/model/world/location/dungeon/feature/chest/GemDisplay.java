package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.precious.Gem;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.Images;

/**
 * @see Gem
 * @see Eidolon
 * @author alex
 */
public class GemDisplay extends Chest{
	/** Constructor. */
	public GemDisplay(Integer pool,DungeonFloor f){
		super(pool,f);
	}

	@Override
	protected boolean allow(Item i){
		return i.is(Gem.class)!=null||i.is(Eidolon.class)!=null;
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","gemdisplay"));
	}
}
