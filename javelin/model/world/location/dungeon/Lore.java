package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.stream.Collectors;

import javelin.controller.challenge.RewardCalculator;
import javelin.controller.table.dungeon.feature.RareFeatureTable;
import javelin.model.item.Item;
import javelin.model.item.key.door.Key;
import javelin.model.world.location.dungeon.feature.Altar;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.LoreNote;

/**
 * A note of interest about a {@link Dungeon} that can later be accessed through
 * a {@link LoreNote}.
 *
 * @author alex
 */
public class Lore implements Serializable{
	/** Dungeon spoiler. */
	public String text;
	/**
	 * A value in gold for the information - might be the value of an {@link Item}
	 * or even {@link Integer#MAX_VALUE} for crucial information. By default a
	 * value around the dungeon's typical treasure encounter is assigned.
	 */
	public int value;
	/**
	 * <code>true</code> if the player has discovered this information. Used to
	 * display information through the LoreScreen.
	 */
	public boolean discovered=false;

	/** Constructor. */
	public Lore(String text,int value){
		this.text=text;
		this.value=value;
	}

	/** Constructor with default {@link #value}. */
	public Lore(String text,Dungeon d){
		this(text,RewardCalculator.getgold(d.level));
	}

	@Override
	public int hashCode(){
		return text.hashCode();
	}

	@Override
	public boolean equals(Object lorep){
		if(!(lorep instanceof Lore)) return false;
		var lore=(Lore)lorep;
		return text.equals(lore.text);
	}

	@Override
	public String toString(){
		return text;
	}

	/** @return Generates {@link Dungeon#lore} for the given floor. */
	public static HashSet<Lore> generate(Dungeon floor){
		var top=floor.floors.get(0);
		var prefix="On floor "+(top.floors.indexOf(floor)+1)+": ";
		var lore=new HashSet<Lore>();
		var monsters=floor.encounters.stream().flatMap(e->e.stream())
				.map(c->c.source).filter(m->!m.elite).collect(Collectors.toList());
		for(var m:monsters)
			lore.add(new Lore(prefix+m,RewardCalculator.getgold(m.cr)));
		var features=floor.features.stream()
				.filter(f->RareFeatureTable.ALL.contains(f.getClass()))
				.collect(Collectors.toList());
		for(var f:features)
			lore.add(new Lore(prefix+f,top));
		var items=floor.features.stream().filter(f->f instanceof Chest)
				.flatMap(c->((Chest)c).items.stream()).filter(i->!(i instanceof Key))
				.collect(Collectors.toList());
		for(var i:items)
			lore.add(new Lore(prefix+i,i.price));
		var altars=floor.features.stream().filter(f->f instanceof Altar)
				.collect(Collectors.toList());
		for(var a:altars)
			lore.add(new Lore(prefix+a,Integer.MAX_VALUE));
		return lore;
	}
}
