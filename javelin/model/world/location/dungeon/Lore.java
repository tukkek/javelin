package javelin.model.world.location.dungeon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.table.dungeon.feature.RareFeatureTable;
import javelin.model.item.Item;
import javelin.model.item.key.door.Key;
import javelin.model.world.location.dungeon.feature.LoreNote;
import javelin.model.world.location.dungeon.feature.chest.ArtifactChest;
import javelin.model.world.location.dungeon.feature.chest.Chest;

/**
 * A note of interest about a {@link DungeonFloor} that can later be accessed
 * through a {@link LoreNote}.
 *
 * @author alex
 */
public class Lore implements Serializable{
	/**
	 * Allow {@link DungeonFloor#lore} to keep all possible entries and for all of
	 * them to be {@link #discovered}. Useful for having quick overview of
	 * dungeons for unrelaed reasons.
	 *
	 * @see Javelin#DEBUG
	 */
	public static final boolean DEBUG=Javelin.DEBUG&&false;

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
	public boolean discovered=Javelin.DEBUG&&DEBUG;

	/** Constructor. */
	public Lore(String text,int value){
		this.text=text;
		this.value=value;
	}

	/** Constructor with default {@link #value}. */
	public Lore(String text,DungeonFloor d){
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

	/** @return Generates {@link DungeonFloor#lore} for the given floor. */
	public static HashSet<Lore> generate(Dungeon d,DungeonFloor floor){
		var top=d;
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
			lore.add(new Lore(prefix+f,floor));
		var items=floor.features.stream().filter(f->f instanceof Chest)
				.flatMap(c->((Chest)c).items.stream()).filter(i->!(i instanceof Key))
				.collect(Collectors.toList());
		for(var i:items)
			lore.add(new Lore(prefix+i,i.price));
		var artifacts=floor.features.stream().filter(f->f instanceof ArtifactChest)
				.collect(Collectors.toList());
		for(var a:artifacts)
			lore.add(new Lore(prefix+a,Integer.MAX_VALUE));
		return lore;
	}
}
