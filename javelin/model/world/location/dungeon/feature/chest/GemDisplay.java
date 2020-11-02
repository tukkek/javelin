package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.precious.Gem;
import javelin.view.Images;

/**
 * @see Gem
 * @author alex
 */
public class GemDisplay extends Chest{
	/** Constructor. */
	public GemDisplay(Integer pool){
		super(pool);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean allow(Item i){
		return i.is(Gem.class)!=null;
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","gemdisplay"));
	}
}
