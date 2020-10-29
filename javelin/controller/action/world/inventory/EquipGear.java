package javelin.controller.action.world.inventory;

import java.util.List;
import java.util.stream.Collectors;

import javelin.model.item.Item;
import javelin.model.item.gear.Gear;

/**
 * {@link Gear} management.
 *
 * @author alex
 */
public class EquipGear extends UseItems{
	/** Singleton. */
	@SuppressWarnings("hiding")
	public static final EquipGear INSTANCE=new EquipGear();

	/** Constructor. */
	EquipGear(){
		super("Equip gear",new int[]{'e'},new String[]{"e"});
	}

	@Override
	protected List<Item> filter(List<Item> items){
		return items.stream().filter(i->i instanceof Gear)
				.collect(Collectors.toList());
	}
}
