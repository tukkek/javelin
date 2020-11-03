package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;
import java.util.Set;

import javelin.model.item.Item;
import javelin.model.item.gear.rune.RuneGear;
import javelin.model.unit.Slot;
import javelin.view.Images;

/**
 * Specialized chest for armor-like {@link RuneGear}.
 *
 * @author alex
 */
public class ArmorDisplay extends Chest{
	static final Set<Slot> SLOTS=Set.of(Slot.FEET,Slot.HANDS,Slot.SHOULDERS,
			Slot.TORSO,Slot.HEAD);

	/** Constructor. */
	public ArmorDisplay(Integer pool){
		super(pool);
	}

	@Override
	protected boolean allow(Item i){
		var g=i instanceof RuneGear?(RuneGear)i:null;
		return g!=null&&SLOTS.contains(g.slot);
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","armordisplay"));
	}
}
