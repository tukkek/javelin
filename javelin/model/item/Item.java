package javelin.model.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.action.UseItem;
import javelin.controller.action.world.UseItems;
import javelin.controller.comparator.ItemsByPrice;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.artifact.CasterRing;
import javelin.model.item.precious.ArtPiece;
import javelin.model.item.precious.Gem;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.fortification.Academy;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Represents an item carried by a {@link Combatant}. Most often items are
 * consumable. Currently only human players use items.
 *
 * When crafting new items, it takes a day per $1000 for the process to
 * complete. Exceptions are {@link Potion}s, which always take 1 day.
 *
 * @author alex
 */
public abstract class Item implements Serializable,Cloneable{
	/**
	 * All available item types from cheapest to most expensive.
	 */
	public static final ItemSelection ALL=new ItemSelection();
	/** Map of items by price in gold coins ($). */
	public static final TreeMap<Integer,ItemSelection> BYPRICE=new TreeMap<>();
	/** Map of items by price {@link Tier} */
	public static final HashMap<Tier,ItemSelection> BYTIER=new HashMap<>();
	/** @see Artifact */
	public static final ItemSelection ARTIFACT=new ItemSelection();
	/** Price of the cheapest {@link Artifact} after loot registration. */
	public static Integer cheapestartifact=null;

	static{
		for(Tier t:Tier.TIERS)
			BYTIER.put(t,new ItemSelection());
	}

	/** Name to show the player. */
	public String name;
	/** Cost in gold pieces. */
	public int price;
	/**
	 * <code>true</code> if can be used during battle . <code>true</code> by
	 * default (default: true).
	 */
	public boolean usedinbattle=true;
	/**
	 * <code>true</code> if can be used while in the world map (default: true).
	 */
	public boolean usedoutofbattle=true;
	/** <code>true</code> if should be expended after use (default: true). */
	public boolean consumable=true;
	/** How many action points to spend during {@link UseItem}. */
	public float apcost=.5f;

	/** Whether to {@link #waste(float, ArrayList)} this item or not. */
	public boolean waste=true;
	/**
	 * Usually only {@link Scroll}s and {@link Potion}s provoke attacks of
	 * opportunity.
	 */
	public boolean provokesaoo=true;
	/** Whether to select a {@link Combatant} to use this on. */
	public boolean targeted=true;

	/**
	 * A value between 0 and 1 signifying how much this item should be (re)sold
	 * for. A value of zero means the item can't be sold.
	 *
	 * Default is 50% original {@link #price}.
	 */
	public double sellvalue=.5f;
	/** If not <code>null</code> will be used for {@link #describefailure()}. */
	volatile protected String failure=null;

	/**
	 * @param upgradeset One the static constants in this class, like
	 *          {@link #MAGIC}.
	 */
	public Item(final String name,final int price,boolean register){
		this.name=name;
		this.price=Javelin.round(price);
		if(register) if(!ALL.add(this)) System.out.println("Discarded: "+this);
	}

	/**
	 * @return A clone of this item (base implementation), with randomized
	 *         parameters, meant for generating a new item to be found in a
	 *         {@link Chest}, for example. Examples being: a used {@link Wand}
	 *         with a certain amount of charges or a {@link Gem} with a random
	 *         sell value.
	 */
	public Item randomize(){
		return clone();
	}

	public void register(){
		BYTIER.get(Tier.get(getlevel())).add(this);
	}

	public int getlevel(){
		final double level=Math.pow(price/7.5,1.0/3.0);
		return Math.max(1,Math.round(Math.round(level)));
	}

	@Override
	public String toString(){
		return name;
	}

	/**
	 * @return <code>true</code> if item was spent.
	 */
	public boolean use(Combatant user){
		throw new RuntimeException("Not used in combat: "+this);
	}

	/**
	 * Uses an item while on the {@link WorldScreen}.
	 *
	 * @param m Unit using the item.
	 * @return <code>true</code> if item is to be expended.
	 */
	public boolean usepeacefully(Combatant user){
		throw new RuntimeException("Not used peacefully: "+this);
	}

	@Override
	public boolean equals(final Object obj){
		return obj instanceof Item&&name.equals(((Item)obj).name);
	}

	@Override
	public int hashCode(){
		return name.hashCode();
	}

	@Override
	public Item clone(){
		try{
			return (Item)super.clone();
		}catch(CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Use this to remove this item from the active {@link Squad}'s inventory.
	 */
	public void expend(){
		/*
		 * needs to catch actual instance not just any item of the same type
		 */
		spend:for(final Combatant owner:Squad.active.members){
			List<Item> items=Squad.active.equipment.get(owner);
			for(Item used:new ArrayList<>(items))
				if(used==this){
					items.remove(used);
					break spend;
				}
		}
	}

	/**
	 * Use this to customize the error message if the item is not expended.
	 */
	public String describefailure(){
		return "Can only be used in battle.";
	}

	/**
	 * @return <code>null</code> if can use this, or an error message otherwise.
	 */
	public String canuse(Combatant c){
		return null;
	}

	/**
	 * Prompts user to select one of the active {@link Squad} members to keep this
	 * item and updates {@link Squad#equipment}.
	 */
	public void grab(Squad s){
		String list=UseItems.listitems(null,false)+"\n";
		list+="Who will take the "+toString().toLowerCase()+"?";
		s.equipment.get(UseItems.selectmember(s.members,this,list)).add(this);
	}

	/**
	 * Same as {@link #grab(Squad)} but uses {@link Squad#active}.
	 */
	public void grab(){
		grab(Squad.active);
	}

	/**
	 * Used as strategic resource damage.
	 *
	 * @param c
	 *
	 * @return Lowercase description of used resources or <code>null</code> if
	 *         wasn't wasted.
	 * @see StartBattle
	 */
	public String waste(float resourcesused,Combatant c,ArrayList<Item> bag){
		if(RPG.random()<resourcesused&&canuse(c)==null){
			bag.remove(this);
			return name;
		}
		return null;
	}

	/** Creates {@link Item}s from {@link Spell}s. */
	public static void setup(){
		for(Spell s:Spell.BYNAME.values()){
			if(s.isscroll) new Scroll(s).register();
			if(s.iswand) new Wand(s).register();
			if(s.ispotion) new Potion(s).register();
			if(s.isring) for(int uses:CasterRing.POWERLEVELS)
				new CasterRing(s,uses).register();
		}
		Gem.generate();
		ArtPiece.generate();
		cheapestartifact=ALL.stream().filter(i->i instanceof Artifact)
				.map(i->i.price).min(Integer::compare).get();
	}

	static void addall(ItemSelection fire2,HashMap<String,ItemSelection> all,
			String string){
		all.put(string,fire2);
	}

	/**
	 * @return A list of all {@link Item}s in any {@link Squad}, {@link Town}
	 *         trainees and {@link Academy} trainees (including subclasses).
	 */
	public static List<Item> getplayeritems(){
		ArrayList<Item> items=new ArrayList<>();
		for(Actor a:World.getactors()){
			Academy academy=a instanceof Academy?(Academy)a:null;
			if(academy!=null){
				for(Order o:academy.training.queue){
					TrainingOrder training=(TrainingOrder)o;
					items.addAll(training.equipment);
				}
				continue;
			}
			Squad squad=a instanceof Squad?(Squad)a:null;
			if(squad!=null){
				squad.equipment.clean();
				for(List<Item> bag:squad.equipment.values())
					items.addAll(bag);
				continue;
			}
		}
		return items;
	}

	/**
	 * @param from A sample of items (like {@link #ALL} or from
	 *          {@link Tier#ITEMS}).
	 * @return The same items but with randomized parameters, from cheapest to
	 *         most expensive (previously shuffled to introduce order randomness
	 *         for items with exact same price).
	 * @see Item#randomize()
	 */
	public static List<Item> randomize(Collection<Item> from){
		ArrayList<Item> randomized=new ArrayList<>(from.size());
		for(Item i:from)
			randomized.add(i.randomize());
		Collections.shuffle(randomized);
		randomized.sort(ItemsByPrice.SINGLETON);
		return randomized;
	}

	/**
	 * @return <code>true</code> if this item can be currently sold.
	 */
	public boolean sell(){
		return sellvalue>0;
	}

	/** @return A human-readable description of this item. */
	public String describe(Combatant c){
		String description=toString();
		String prohibited=canuse(c);
		if(prohibited!=null)
			description+=" ("+prohibited+")";
		else if(c.equipped.contains(this)) description+=" (equipped)";
		return description;
	}

	/**
	 * @return <code>true</code> if any of the {@link Combatant}s can use this.
	 * @see #canuse(Combatant)
	 */
	public boolean canuse(List<Combatant> members){
		for(var member:members)
			if(canuse(member)==null) return true;
		return false;
	}
}
