package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.gear.rune.Rune;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.Images;

/**
 * @see Scroll
 * @see Rune
 * @author alex
 */
public class Bookcase extends Chest{
	public Bookcase(Integer gold,DungeonFloor f){
		super(gold,f);
	}

	@Override
	protected boolean allow(Item i){
		return i.is(Scroll.class)!=null||i.is(Rune.class)!=null;
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","bookcase"));
	}
}
