package javelin.controller.content.action.world.inventory;

import java.util.List;
import java.util.stream.Collectors;

import javelin.model.item.Item;
import javelin.model.item.gear.Gear;
import javelin.model.item.gear.rune.RuneGear;
import javelin.view.screen.InfoScreen;

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
		stayopen=true;
	}

	@Override
	protected List<Item> filter(List<Item> items){
		return items.stream().filter(i->i instanceof Gear)
				.collect(Collectors.toList());
	}

	@Override
	boolean use(InfoScreen infoscreen,Item i){
		var g=(RuneGear)i;
		return g.equip(findowner(g));
	}
}
