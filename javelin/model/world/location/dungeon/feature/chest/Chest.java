package javelin.model.world.location.dungeon.feature.chest;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Decoration;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * The base chest implementation - can contain anything and should be used as a
 * fallback when specialized chests fail.
 *
 * @author alex
 */
public class Chest extends Feature{
	/**
	 * Attempts to generate items via {@link Item#randomize()} before giving up
	 * that one will fit a given {@link #generateitem()} {@link #gold} range. A
	 * number 10x this size doesn't seem to produce any better results.
	 *
	 * Impact on performance seems non-noticeable and the {@link #generateitem()}
	 * implementation does its best to be graceful about it and to handle
	 * best-case scenarios gracefully.
	 *
	 * This has an impact of reducing cases where subclasses need to fallback to
	 * the basic chest implementation from 1/2 cases to 1/3 and up to 1/5.
	 */
	static final int GENERATIONATTEMPTS=1000;

	/** Items inside the chest. */
	public ItemSelection items=new ItemSelection();
	/** Gold inside the chest. */
	public int gold=0;

	/**
	 * {@link Skill#PERCEPTION} difficulty class or <code>null</code> if not
	 * hidden.
	 *
	 * @see Decoration#hide(Feature)
	 */
	Integer searchdc;

	/**
	 * Constructor.
	 *
	 * @param f
	 */
	Chest(DungeonFloor f){
		super("chest");
		searchdc=10+f.level+f.gettable(FeatureModifierTable.class).roll();
	}

	/** Construtor with {@link #gold} pool. */
	public Chest(Integer gold,DungeonFloor f){
		this(f);
		if(gold<1) gold=1;
		this.gold=Javelin.round(gold);
	}

	/** Constructor with {@link #items}. */
	public Chest(Item i,DungeonFloor f){
		this(f);
		items.add(i);
	}

	/**
	 * Converts {@link #gold} to container-appropriate {@link Item}s.
	 *
	 * @return <code>true</code> if generated any {@link #items}.
	 */
	public boolean generateitem(){
		var range=new ArrayList<>(List.of(RPG.randomize(gold,0,Integer.MAX_VALUE),
				RPG.randomize(gold,0,Integer.MAX_VALUE)));
		range.sort(null);
		var candidates=new ArrayList<>(
				Item.ITEMS.stream().filter(i->allow(i)).collect(Collectors.toList()));
		Item item=null;
		for(var j=0;j<GENERATIONATTEMPTS&&item==null;j++)
			item=RPG.shuffle(candidates).stream().map(i->i.randomize())
					.filter(i->range.get(0)<=i.price&&i.price<=range.get(1)).findAny()
					.orElse(null);
		if(item==null) return false;
		gold=0;
		items.add(item);
		return true;
	}

	@Override
	public boolean activate(){
		if(items.isEmpty()){
			if(gold<1) gold=1;
			String message="Party receives $"+Javelin.format(gold)+"!";
			Javelin.message(message,false);
			Squad.active.gold+=gold;
		}else
			for(Item i:items)
				i.grab();
		return true;
	}

	@Override
	public String toString(){
		return getClass().getSimpleName()+": "
				+(items.isEmpty()?"$"+Javelin.format(gold):items);
	}

	@Override
	public Image getimage(){
		var i=Dungeon.active.dungeon.images.get(DungeonImages.CHEST);
		return Images.get(List.of("dungeon","chest",i));
	}

	/** @return <code>true</code> if the item is allowed on this container. */
	protected boolean allow(Item i){
		return true;
	}

	@Override
	public boolean discover(Combatant searching,int searchroll){
		return searchroll>=searchdc;
	}
}
