package javelin.model.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.action.UseItem;
import javelin.controller.action.world.inventory.EquipGear;
import javelin.controller.action.world.inventory.UseItems;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.Healing;
import javelin.model.item.consumable.Eidolon;
import javelin.model.item.consumable.Scroll;
import javelin.model.item.focus.Rod;
import javelin.model.item.focus.Staff;
import javelin.model.item.focus.Wand;
import javelin.model.item.gear.Gear;
import javelin.model.item.gear.rune.Rune;
import javelin.model.item.gear.rune.RuneGear;
import javelin.model.item.potion.Flask;
import javelin.model.item.potion.Potion;
import javelin.model.item.precious.ArtPiece;
import javelin.model.item.precious.Gem;
import javelin.model.item.precious.PreciousObject;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.conjuration.healing.wounds.CureLightWounds;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.Academy;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Represents an item carried by a {@link Combatant}. Most often items are
 * consumable. Currently only human players use items.
 *
 * When crafting new items, it takes a day per $1000 for the process to
 * complete. Exceptions are {@link Potion}s, which always take 1 day.
 *
 * Any {@link Item}s that allow for unlimited casting of {@link Spell}s needs to
 * be {@link Recharger}-based, otherwise access to a single
 * {@link CureLightWounds} will trivialize things like travel or
 * {@link DungeonFloor}s.
 *
 * @author alex
 */
public abstract class Item implements Serializable,Cloneable,Healing{
	/**
	 * All available item types from cheapest to most expensive.
	 */
	public static final ItemSelection ITEMS=new ItemSelection();
	/** Map of items by price in gold coins ($). */
	public static final TreeMap<Integer,ItemSelection> BYPRICE=new TreeMap<>();
	/** Map of items by price {@link Tier} */
	public static final HashMap<Tier,ItemSelection> BYTIER=new HashMap<>();
	/** @see Gear */
	public static final ItemSelection ARTIFACT=new ItemSelection();
	/** All utility items (not {@link PreciousObject}s). */
	public static final ItemSelection NONPRECIOUS=new ItemSelection();

	/** Price of the cheapest {@link Gear} after loot registration. */
	public static Integer cheapestartifact=null;

	/** If not <code>null</code> will be used for {@link #describefailure()}. */
	static public String failure=null;

	static{
		for(Tier t:Tier.TIERS)
			BYTIER.put(t,new ItemSelection());
	}

	/**
	 * Creates {@link Item}s from {@link Spell}s. Base constructor will add to
	 * {@link #ITEMS}.
	 */
	@SuppressWarnings("unused")
	public static void setup(){
		for(Spell s:Spell.BYNAME.values()){
			if(s.ispotion){
				new Potion(s);
				for(int dailyuses:Flask.VARIATIONS)
					new Flask(s,dailyuses);
			}
			//spell-completion
			if(s.iswand)
				if(s.level<=Wand.MAXLEVEL)
					new Wand(s);
				else
					new Staff(s);
			else if(s.isscroll&&!s.provokeaoo) new Scroll(s);
			//use-activated
			if(s.isrod) new Rod(s);
		}
		Gem.generate();
		ArtPiece.generate();
		Eidolon.generate();
		RuneGear.generate();
		Rune.generate();
		for(var i=0;i<ITEMS.size();i++)
			ITEMS.set(i,ITEMS.get(i).randomize());
		for(var i:ITEMS)
			BYTIER.get(Tier.get(i.getlevel())).add(i);
		cheapestartifact=ITEMS.stream().filter(i->i instanceof Gear).map(i->i.price)
				.min(Integer::compare).get();
		NONPRECIOUS.addAll(ITEMS.stream().filter(i->!(i instanceof PreciousObject))
				.collect(Collectors.toList()));
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
	/**
	 * <code>true</code> by default, only one-use {@link #consumable} items should
	 * use this mechanic, as they can still be used with their true identity
	 * hidden by {@link #toString()} and it won't ever lock the player away
	 * regardless of game-state circumstances.
	 *
	 * {@link #toString()} will use the class name as a guess for what to call
	 * non-identified items but subclasses should provide their method override
	 * when that is not enough.
	 *
	 * @see Squad#identify(Item)
	 * @see Lodge#rest(int, int, boolean,
	 *      javelin.model.world.location.town.labor.basic.Lodge.Lodging)
	 */
	public boolean identified=true;

	/**
	 * Constructor.
	 *
	 * @param price Some price calculations require divisions, which are performed
	 *          wrong by Java if you only use {@link Integer}s. This being a
	 *          {@link Double} makes it easier to just declare real numbers in
	 *          such formulas, even though the final result will be passed to
	 *          {@link #setprice(double)} and thus made into an {@link Integer}.
	 * @param register If <code>true</code> will {@link #register()}. Subclasses
	 *          amy choose to do that later on with <code>false</code>.
	 */
	public Item(final String name,double price,boolean register){
		this.name=name;
		setprice(price);
		if(register) ITEMS.add(this);
	}

	/**
	 * @param price Will round this to an {@link Integer} and
	 *          {@link Javelin#round(int)}, replacing #{@link #price}.
	 */
	protected void setprice(double price){
		this.price=Javelin.round(Math.round(Math.round(price)));
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

	public int getlevel(){
		final double level=Math.pow(price/7.5,1.0/3.0);
		return Math.max(1,Math.round(Math.round(level)));
	}

	@Override
	public String toString(){
		if(!identified)
			return "Unidentified "+getClass().getSimpleName().toLowerCase();
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
	 * @see #failure
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
	 * Use this to remove a particular instance from the active
	 * {@link Squad#equipment} (not an equivalent item, ie
	 * {@link #equals(Object)}).
	 */
	public void expend(){
		for(var c:Squad.active.members){
			var bag=Squad.active.equipment.get(c);
			for(var i:new ArrayList<>(bag))
				if(i==this){
					bag.remove(i);
					return;
				}
		}
	}

	/**
	 * Use this to customize the error message if the item is not expended.
	 */
	public String describefailure(){
		return failure;
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
		identified=s.identify(this);
		var action=this instanceof Gear?EquipGear.INSTANCE:UseItems.INSTANCE;
		String list=action.listitems(null,false)+"\n";
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
		if(RPG.random()>=resourcesused||canuse(c)!=null) return null;
		bag.remove(this);
		return name;
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
	 * @param from A sample of items (like {@link #ITEMS} or from
	 *          {@link #BYTIER}).
	 * @return A new, shuffled list with the same items but with
	 *         {@link Item#randomize()} instances.
	 */
	public static List<Item> randomize(Collection<Item> from){
		var randomized=new ArrayList<Item>(from.size());
		for(var i:from)
			randomized.add(i.randomize());
		Collections.shuffle(randomized);
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
			description+=" ("+prohibited.toLowerCase()+")";
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

	/**
	 * Items with daily uses may refresh charges.
	 *
	 * @param hours Hours ellapsed.
	 */
	public void refresh(int hours){
		//most items do not refresh
	}

	/**
	 * @return List with all {@link Item#ITEMS} of the given class after
	 *         {@link #randomize()}.
	 */
	public static <K> List<K> getall(Class<K> type){
		return randomize(Item.ITEMS).stream().filter(i->type.isInstance(i))
				.map(i->(K)i).collect(Collectors.toList());
	}

	/**
	 * @return This item instance but type-cast or <code>null</code> if not the
	 *         given item type.
	 */
	@SuppressWarnings("unchecked")
	public <K extends Item> K is(Class<K> type){
		return type.isInstance(this)?(K)this:null;
	}

	@Override
	public boolean canheal(Combatant c){
		return false;
	}

	@Override
	public void heal(Combatant c){
		//doesn't by default
	}

	@Override
	public int getheals(){
		return 0;
	}
}
