package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;
import java.util.Set;

import javelin.model.item.Item;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.gear.rune.Rune;
import javelin.model.item.potion.Flask;
import javelin.model.item.potion.Potion;
import javelin.view.Images;

/**
 * Specialized chest for consumable and miscellaneous items.
 *
 * @author alex
 */
public class Backpack extends Chest{
	static final Set<Class<? extends Item>> ALLOWED=Set.of(Eidolon.class,
			Potion.class,Flask.class,Rune.class);

	/** Constructor. */
	public Backpack(Integer gold){
		super(gold);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean allow(Item i){
		return ALLOWED.contains(i.getClass());
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","backpack"));
	}
}
