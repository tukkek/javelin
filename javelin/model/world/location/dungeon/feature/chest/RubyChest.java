package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.model.item.consumable.Ruby;
import javelin.view.Images;

/**
 * @see Ruby
 * @author alex
 */
public class RubyChest extends Chest{
	/** Constructor. */
	public RubyChest(){
		super(new Ruby());
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","ruby"));
	}
}
