package javelin.model.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.action.UseItem;
import javelin.controller.action.world.UseItems;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.upgrade.Spell;
import javelin.model.Realm;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.shopping.ShoppingScreen;
import tyrant.mikera.engine.RPG;

/**
 * Represents an item carried by a {@link Combatant}. Most often items are
 * consumable. Currently only human players use items.
 *
 * When crafting new items, it takes a day per $1000 for the process to
 * complete. Exceptions are {@link Potion}s, which always take 1 day.
 *
 * @author alex
 */
public abstract class Item implements Serializable, Cloneable {
	/**
	 * Organizes a {@link List} from cheapest to most expensive Item.
	 *
	 * @see List#sort(Comparator)
	 */
	public static Comparator<Item> PRICECOMPARATOR = new Comparator<Item>() {
		@Override
		public int compare(Item o1, Item o2) {
			return o1.price - o2.price;
		}
	};

	/**
	 * All available item types from cheapest to most expensive.
	 */
	public static final ItemSelection ALL = new ItemSelection();
	/** Map of items by price in gold coins ($). */
	public static final TreeMap<Integer, ItemSelection> BYPRICE = new TreeMap<Integer, ItemSelection>();
	/** @see Item#getselection(Realm) */
	public static final ItemSelection FIRE = new ItemSelection();
	/** @see Item#getselection(Realm) */
	public static final ItemSelection WIND = new ItemSelection();
	/** @see Item#getselection(Realm) */
	public static final ItemSelection EARTH = new ItemSelection();
	/** @see Item#getselection(Realm) */
	public static final ItemSelection WATER = new ItemSelection();
	/** @see Item#getselection(Realm) */
	public static final ItemSelection GOOD = new ItemSelection();
	/** @see Item#getselection(Realm) */
	public static final ItemSelection EVIL = new ItemSelection();
	/** @see Item#getselection(Realm) */
	public static final ItemSelection MAGIC = new ItemSelection();
	/** @see Artifact */
	public static final ItemSelection ARTIFACT = new ItemSelection();

	/** Creates {@link Item}s from {@link Spell}s. */
	public static void init() {
		for (Spell s : Spell.SPELLS.values()) {
			if (s.isscroll) {
				new Scroll(s);
			}
			if (s.iswand) {
				new Wand(s);
			}
			if (s.ispotion) {
				new Potion(s);
			}
		}

		mapbyprice();
	}

	/** Name to be shown the player. */
	public String name;
	/** Cost in gold pieces. */
	public int price;
	/**
	 * <code>true</code> if can be used during battle . <code>true</code> by
	 * default (default: true).
	 */
	public boolean usedinbattle = true;
	/**
	 * <code>true</code> if can be used while in the world map (default: true).
	 */
	public boolean usedoutofbattle = true;
	/** <code>true</code> if should be expended after use (default: true). */
	public boolean consumable = true;

	/** If not <code>null</code> will be used for {@link #describefailure()}. */
	volatile protected String failure = null;
	/** How many action points to spend during {@link UseItem}. */
	public float apcost = .5f;

	/**
	 * @param upgradeset
	 *            One the static constants in this class, like {@link #MAGIC}.
	 */
	public Item(final String name, final int price,
			final ItemSelection upgradeset) {
		this.name = name;
		this.price = price;
		if (upgradeset != null) {
			ALL.add(this);
			upgradeset.add(this);
		}
	}

	/** Sorts {@link #ALL} by price. */
	public static void mapbyprice() {
		Collections.shuffle(ALL);
		Collections.sort(ALL, PRICECOMPARATOR);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return <code>true</code> if item was spent.
	 */
	public boolean use(Combatant user) {
		throw new RuntimeException("Not used in combat: " + this);
	}

	/**
	 * Uses an item while on the {@link WorldScreen}.
	 *
	 * @param m
	 *            Unit using the item.
	 * @return <code>true</code> if item is to be expended.
	 */
	public boolean usepeacefully(Combatant user) {
		throw new RuntimeException("Not used peacefully: " + this);
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Item ? name.equals(((Item) obj).name) : false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public Item clone() {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	// /**
	// * Fill the {@link Town} shops with {@link Item} at the start of the
	// * campaign.
	// */
	// public static void distribute() {
	// CureLightWounds curelightwounds = new CureLightWounds();
	// for (WorldActor p : Location.getall(Town.class)) {
	// Town t = (Town) p;
	// t.items.add(new Potion(new CureLightWounds()));
	// int nitems = RPG.r(3 - 1, 5 - 1);
	// ItemSelection selection = new ItemSelection(getselection(t.realm));
	// selection.remove(curelightwounds);
	// if (nitems > selection.size()) {
	// nitems = selection.size();
	// }
	// while (t.items.size() - 1 < nitems
	// && t.items.size() != selection.size()) {
	// t.items.add(selection.random());
	// }
	// for (Item i : t.items) {
	// i.shop();
	// }
	// Collections.sort(t.items, Item.PRICECOMPARATOR);
	// }
	// }

	/**
	 * Each {@link Item} is assigned a {@link Realm} on creation. This
	 * determines what type of item each {@link Town} can produce.
	 *
	 * @see Item#Item(String, int, ItemSelection)
	 * @param r
	 *            Given a realm...
	 * @return all {@link Item}s assigned to that realm.
	 */
	public static ItemSelection getselection(Realm r) {
		if (r == Realm.AIR) {
			return WIND;
		} else if (r == Realm.FIRE) {
			return FIRE;
		} else if (r == Realm.EARTH) {
			return EARTH;
		} else if (r == Realm.WATER) {
			return WATER;
		} else if (r == Realm.GOOD) {
			return GOOD;
		} else if (r == Realm.EVIL) {
			return EVIL;
		} else if (r == Realm.MAGIC) {
			return MAGIC;
		} else {
			throw new RuntimeException("Unknown realm!");
		}
	}

	/**
	 * Use this to remove this item from the active {@link Squad}'s inventory.
	 */
	public void expend() {
		/*
		 * needs to catch actual instance not just any item of the same type
		 */
		spend: for (final Combatant owner : Squad.active.members) {
			List<Item> items = Squad.active.equipment.get(owner.id);
			for (Item used : new ArrayList<Item>(items)) {
				if (used == this) {
					items.remove(used);
					break spend;
				}
			}
		}
	}

	/**
	 * Use this to customize the error message if the item is not expended.
	 */
	public String describefailure() {
		return "Can only be used in battle.";
	}

	/**
	 * @return All items types mapped by realm.
	 */
	public static HashMap<String, ItemSelection> getall() {
		HashMap<String, ItemSelection> all = new HashMap<String, ItemSelection>();
		addall(FIRE, all, "fire");
		addall(EARTH, all, "earth");
		addall(WATER, all, "water");
		addall(WIND, all, "wind");

		addall(GOOD, all, "good");
		addall(EVIL, all, "evil");
		addall(MAGIC, all, "magic");

		addall(ARTIFACT, all, "artifact");
		return all;
	}

	static private void addall(ItemSelection fire2,
			HashMap<String, ItemSelection> all, String string) {
		all.put(string, fire2);
	}

	/**
	 * @return <code>null</code> if can use this, or an error message otherwise.
	 */
	public String canuse(Combatant c) {
		return null;
	}

	/**
	 * Called if this is instance is going to be sold and cloned by a town's
	 * {@link ShoppingScreen}.
	 *
	 * @see #clone()
	 */
	public void shop() {
		// does nothing
	}

	/**
	 * Prompts user to select one of the active {@link Squad} members to keep
	 * this item and updates {@link Squad#equipment}.
	 */
	public void grab(Squad s) {
		final String list = UseItems.listitems(null, false)
				+ "\nWho will take the " + toString().toLowerCase() + "?";
		s.equipment.get(UseItems.selectmember(s.members, this, list).id)
				.add(this);
	}

	/**
	 * Same as {@link #grab(Squad)} but uses {@link Squad#active}.
	 */
	public void grab() {
		grab(Squad.active);
	}

	/**
	 * @return A list of all {@link Item}s in any {@link Squad}, {@link Town}
	 *         trainees and {@link Academy} trainees (including subclasses).
	 */
	public static List<Item> getplayeritems() {
		ArrayList<Item> items = new ArrayList<Item>();
		for (Actor a : World.getall()) {
			Academy academy = a instanceof Academy ? (Academy) a : null;
			if (academy != null) {
				for (Order o : academy.training.queue) {
					TrainingOrder training = (TrainingOrder) o;
					items.addAll(training.equipment);
				}
				continue;
			}
			Squad squad = a instanceof Squad ? (Squad) a : null;
			if (squad != null) {
				squad.equipment.clean(squad);
				for (List<Item> bag : squad.equipment.values()) {
					items.addAll(bag);
				}
				continue;
			}
		}
		return items;
	}

	/**
	 * Used as strategic resource damage.
	 *
	 * @return Lowercase description of used resources or empty string.
	 * @see StartBattle
	 */
	public String waste(float resourcesused, ArrayList<Item> bag) {
		if (RPG.random() < resourcesused) {
			bag.remove(this);
			return name;
		}
		return "";
	}
}
