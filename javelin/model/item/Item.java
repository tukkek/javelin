package javelin.model.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.db.StateManager;
import javelin.model.Realm;
import javelin.model.item.artifact.Artifact;
import javelin.model.item.potion.Darkvision;
import javelin.model.item.potion.Fly;
import javelin.model.item.potion.Heroism;
import javelin.model.item.potion.Potion;
import javelin.model.item.potion.barkskin.Barkskin2;
import javelin.model.item.potion.barkskin.Barkskin3;
import javelin.model.item.potion.barkskin.Barkskin4;
import javelin.model.item.potion.barkskin.Barkskin5;
import javelin.model.item.potion.cure.CureCriticalWounds;
import javelin.model.item.potion.cure.CureLightWounds;
import javelin.model.item.potion.cure.CureModerateWounds;
import javelin.model.item.potion.cure.CureSeriousWounds;
import javelin.model.item.potion.resistenergy.ResistEnergy2;
import javelin.model.item.potion.resistenergy.ResistEnergy4;
import javelin.model.item.potion.resistenergy.ResistEnergy6;
import javelin.model.item.potion.totem.BearsEndurance;
import javelin.model.item.potion.totem.EaglesSplendor;
import javelin.model.item.potion.totem.FoxsCunning;
import javelin.model.item.potion.totem.OwlsWisdom;
import javelin.model.item.scroll.PryingEyes;
import javelin.model.item.scroll.RaiseScroll;
import javelin.model.item.scroll.RecallScroll;
import javelin.model.item.scroll.RessurectScroll;
import javelin.model.item.scroll.SecureShelterScroll;
import javelin.model.item.scroll.Teleport;
import javelin.model.item.scroll.dungeon.DiscernLocation;
import javelin.model.item.scroll.dungeon.LocateObject;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.town.Town;
import javelin.view.screen.WorldScreen;
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
	public static final TreeMap<Integer, ItemSelection> BYPRICE =
			new TreeMap<Integer, ItemSelection>();
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
	public static final ItemSelection MAGIC = new ItemSelection();
	/** @see Artifact */
	public static final ItemSelection ARTIFACT = new ItemSelection();

	public static void init() {
		new CureLightWounds();
		new CureModerateWounds();
		new CureSeriousWounds();
		new CureCriticalWounds();
		new Barkskin2();
		new Barkskin3();
		new Barkskin4();
		new Barkskin5();
		new ResistEnergy2();
		new ResistEnergy4();
		new ResistEnergy6();
		new Darkvision();
		new Fly();
		new Heroism();//
		new BearsEndurance();
		new javelin.model.item.potion.totem.BullsStrength();
		new javelin.model.item.potion.totem.CatsGrace();
		new EaglesSplendor();
		new FoxsCunning();
		new OwlsWisdom();

		new DiscernLocation();
		new LocateObject();
		new PryingEyes();
		new RaiseScroll();
		new RecallScroll();
		new RessurectScroll();
		new SecureShelterScroll();
		new Teleport();
		mapbyprice();
	}

	public String name;
	public int price;
	public boolean usedinbattle = true;
	public boolean consumable = true;

	public Item(final String name, final int price, final ItemSelection town) {
		super();
		this.name = name;
		this.price = price;
		ALL.add(this);
		if (town != null) {
			town.add(this);
		}
	}

	public static void mapbyprice() {
		Collections.shuffle(ALL);
		Collections.sort(ALL, PRICECOMPARATOR);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return <code>true</code> if item was used.
	 */
	public abstract boolean use(Combatant user);

	/**
	 * Uses an item while on the {@link WorldScreen}.
	 * 
	 * @param m
	 *            Unit using the item.
	 * @return <code>true</code> if item is to be expended.
	 */
	public abstract boolean usepeacefully(Combatant c);

	@Override
	public boolean equals(final Object obj) {
		/**
		 * TODO this is probably a bad idea, better to have a #contains(Class<?
		 * extends Item>) on ItemSelection or something like this.
		 * 
		 * See the need for Artifact#equals for example and silly classes like
		 * new Barkskin3()
		 */
		return getClass().equals(obj.getClass());
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Fill the {@link Town} shops with {@link Item} at the start of the
	 * campaign.
	 */
	public static void distribute() {
		CureLightWounds curelightwounds = new CureLightWounds();
		for (WorldActor p : WorldPlace.getall(Town.class)) {
			Town t = (Town) p;
			t.items.add(curelightwounds);
			int nitems = RPG.r(3 - 1, 5 - 1);
			ItemSelection selection = new ItemSelection(getselection(t.realm));
			selection.remove(curelightwounds);
			if (nitems > selection.size()) {
				nitems = selection.size();
			}
			while (t.items.size() - 1 < nitems) {
				t.items.add(selection.random());
			}
			Collections.sort(t.items, Item.PRICECOMPARATOR);
		}
	}

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
		if (r == Realm.WIND) {
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
		} else if (r == Realm.MAGICAL) {
			return MAGIC;
		} else {
			throw new RuntimeException("Unknown town!");
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
		StateManager.save();
	}

	public String describefailure() {
		return "Can only be used in battle.";
	}

	public static HashMap<String, ItemSelection> getall() {
		HashMap<String, ItemSelection> all =
				new HashMap<String, ItemSelection>();
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
}
