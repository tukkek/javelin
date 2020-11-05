package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;
import java.util.Set;

import javelin.model.item.Item;
import javelin.model.item.potion.Flask;
import javelin.model.item.potion.Potion;
import javelin.view.Images;

/**
 * TODO change for more "thematic" container type
 *
 * @author alex
 */
public class DeadAdventurer extends Chest{
	static final Set<Class<? extends Item>> ALLOWED=Set.of(Potion.class,
			Flask.class);

	/** Constructor. */
	public DeadAdventurer(Integer gold){
		super(gold);
	}

	@Override
	protected boolean allow(Item i){
		return ALLOWED.contains(i.getClass());
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","deadadventurer"));
	}
}
