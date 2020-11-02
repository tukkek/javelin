package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;
import java.util.Set;

import javelin.model.item.Item;
import javelin.model.item.gear.rune.RuneGear;
import javelin.model.unit.Slot;
import javelin.view.Images;

/**
 * Chest for miscellaneous {@link RuneGear}.
 *
 * TODO could also be a trap once we have the Undead template, or even with NPC
 * skeletons...?
 *
 * @author alex
 */
public class DeadAdventurer extends Chest{
	private static final Set<Slot> SLOTS=Set.of(Slot.EYES,Slot.FINGERS,Slot.NECK,
			Slot.WAIST,Slot.SLOTLESS);

	/** Constructor. */
	public DeadAdventurer(Integer pool){
		super(pool);
	}

	@Override
	protected boolean allow(Item i){
		var g=i instanceof RuneGear?(RuneGear)i:null;
		return g!=null&&SLOTS.contains(g.slot);
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","deadadventurer"));
	}
}
