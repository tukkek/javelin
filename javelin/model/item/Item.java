package javelin.model.item;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.db.StateManager;
import javelin.model.item.potion.Darkvision;
import javelin.model.item.potion.Fly;
import javelin.model.item.potion.Heroism;
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
import javelin.model.item.potion.totem.OwlsWisdom;
import javelin.model.item.scroll.RaiseScroll;
import javelin.model.item.scroll.RecallScroll;
import javelin.model.item.scroll.RessurectScroll;
import javelin.model.item.scroll.SecureShelterScroll;
import javelin.model.item.scroll.Teleport;
import javelin.model.item.scroll.dungeon.DiscernLocation;
import javelin.model.item.scroll.dungeon.LocateObject;
import javelin.model.item.scroll.dungeon.PryingEyes;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.model.world.WorldPlace;
import tyrant.mikera.engine.RPG;

/**
 * Represents an item carried by a {@link Combatant}. Most often items are
 * consumable. Currently only human players use items.
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
	public static final ItemSelection MINOR = new ItemSelection();
	public static final ItemSelection MEDIUM = new ItemSelection();
	public static final ItemSelection MAJOR = new ItemSelection();
	public static final ItemSelection FIRE = new ItemSelection();
	public static final ItemSelection WIND = new ItemSelection();
	public static final ItemSelection EARTH = new ItemSelection();
	public static final ItemSelection WATER = new ItemSelection();
	public static final ItemSelection GOOD = new ItemSelection();
	public static final ItemSelection EVIL = new ItemSelection();
	public static final ItemSelection MAGIC = new ItemSelection();

	public String name;
	public int price;

	static {
		new CureLightWounds();
		new CureModerateWounds();
		new CureSeriousWounds();
		new CureCriticalWounds();
		new RaiseScroll();
		new RessurectScroll();
		new SecureShelterScroll();
		new RecallScroll();
		new Barkskin2();
		new Barkskin3();
		new Barkskin4();
		new Barkskin5();
		new ResistEnergy2();
		new ResistEnergy4();
		new ResistEnergy6();
		new Darkvision();
		new Heroism();//
		new Fly();
		new BearsEndurance();
		new javelin.model.item.potion.totem.BullsStrength();
		new javelin.model.item.potion.totem.CatsGrace();
		new OwlsWisdom();
		new EaglesSplendor();
		new LocateObject();
		new PryingEyes();
		new DiscernLocation();
		new Teleport();
		mapbyprice();
	}

	public Item(final String name, final int price, final ItemSelection town) {
		super();
		this.name = name;
		this.price = price;
		ALL.add(this);
		if (town != null) {
			town.add(this);
		}
	}

	private static void mapbyprice() {
		Collections.shuffle(ALL);
		Collections.sort(ALL, PRICECOMPARATOR);
		int pertier = ALL.size() / 3;
		int i = 0;
		for (; i <= pertier; i++) {
			MINOR.add(ALL.get(i));
		}
		for (; i <= 2 * pertier; i++) {
			MEDIUM.add(ALL.get(i));
		}
		for (; i < ALL.size(); i++) {
			MAJOR.add(ALL.get(i));
		}
		for (Item it : Item.ALL) {
			ItemSelection l = BYPRICE.get(it.price);
			if (l == null) {
				l = new ItemSelection();
				BYPRICE.put(it.price, l);
			}
			l.add(it);
		}
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return <code>true</code> if item was used.
	 */
	public abstract boolean use(Combatant user);

	public boolean canuseneganged() {
		return false;
	}

	public abstract boolean isusedinbattle();

	public abstract boolean usepeacefully(Combatant m);

	@Override
	public boolean equals(final Object obj) {
		try {
			return ((Item) obj).name.equals(name);
		} catch (final ClassCastException e) {
			return false;
		}
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
		int maxoptions = 0;
		for (Town t : Town.towns) {
			final ItemSelection inventory;
			if (t.color == null) {
				inventory = WIND;
			} else if (t.color == Color.red) {
				inventory = FIRE;
			} else if (t.color == Color.green) {
				inventory = EARTH;
			} else if (t.color == Color.BLUE) {
				inventory = WATER;
			} else if (t.color == Color.white) {
				inventory = GOOD;
			} else if (t.color == Color.BLACK) {
				inventory = EVIL;
			} else if (t.color == Color.MAGENTA) {
				inventory = MAGIC;
			} else {
				throw new RuntimeException("Unknown town!");
			}
			t.items.addAll(inventory);
			if (!t.items.contains(curelightwounds)) {
				t.items.add(curelightwounds);
			}
			maxoptions =
					Math.max(maxoptions, t.items.size() + t.upgrades.size());
		}
		for (Town t : Town.towns) {
			int minorcount = 0;
			int mediumcount = 0;
			int majorcount = 0;
			for (Item i : t.items) {
				if (MINOR.contains(i)) {
					minorcount += 1;
				} else if (MEDIUM.contains(i)) {
					mediumcount += 1;
				} else if (MAJOR.contains(i)) {
					majorcount += 1;
				} else {
					throw new RuntimeException("Item not tiered");
				}
			}
			while (t.items.size() + t.upgrades.size() < maxoptions) {
				ItemSelection addfrom = null;
				if (minorcount < mediumcount || minorcount < majorcount) {
					minorcount += 1;
					addfrom = MINOR;
				} else if (mediumcount < majorcount) {
					mediumcount += 1;
					addfrom = MEDIUM;
				} else {
					majorcount += 1;
					addfrom = MAJOR;
				}
				while (!t.items.add(RPG.pick(addfrom))) {
					continue;// already has this item
				}
			}
			Collections.sort(t.items, Item.PRICECOMPARATOR);
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

	/**
	 * Called when a item has finished crafting.
	 * 
	 * @param town
	 *            {@link WorldPlace} this item is being completed on.
	 */
	public void produce(Town town) {
		// your order is ready, m'lord
	}
}
