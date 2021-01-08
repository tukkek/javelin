package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.List;

import javelin.model.item.Item;
import javelin.model.item.gear.rune.Rune;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.Images;

/** @see Rune */
public class RuneStand extends Chest{
	/** Constructor. */
	public RuneStand(Integer gold,DungeonFloor f){
		super(gold,f);
	}

	@Override
	protected boolean allow(Item i){
		return i instanceof Rune;
	}

	@Override
	public Image getimage(){
		return Images.get(List.of("dungeon","chest","runestand"));
	}
}
