package javelin.model.world.place.town.research;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.UpgradeHandler;
import javelin.model.Realm;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Monster;
import javelin.model.world.WorldActor;
import javelin.model.world.place.town.Accommodations;
import javelin.model.world.place.town.ResearchData;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.town.Transport;
import javelin.view.screen.Option;
import javelin.view.screen.town.ResearchScreen;
import javelin.view.screen.town.option.RecruitOption;
import tyrant.mikera.engine.RPG;

/**
 * Represents a way in which a {@link Town} can be improved.
 * 
 * It's a "card-game" mini-game so each town {@link #draw(Town)} cards and has
 * to use one before drawing another.
 * 
 * @see ResearchData
 * @author alex
 */
public abstract class Research extends Option {
	private static final Research[] SPECIALCARDS =
			new Research[] { new Redraw(), new Discard() };
	static final int MAXGROWTH = Integer.MAX_VALUE;

	/**
	 * <code>false</code> if the AI (opponent automanager) should not use this
	 * card.
	 * 
	 * @see Town#automanage
	 */
	public boolean aiable = true;

	/**
	 * <code>true</code> if requires immediate user input, and cannot be queued.
	 * Usually these will also not be {@link #aiable}.
	 */
	public boolean immediate = false;

	public Research(String name, double d, Character keyp) {
		super(name, d, keyp);
	}

	public Research(String name, double price) {
		super(name, price);
	}

	@Override
	public String toString() {
		return name.toLowerCase();
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Will be called on {@link #finish(Town)}.
	 * 
	 * @param t
	 *            Town to be improved.
	 * @param s
	 *            Could be <code>null</code> if this is not tagged as
	 *            {@link #immediate}.
	 */
	public abstract void apply(Town t, ResearchScreen s);

	/**
	 * Draws one card to the played building hand.
	 * 
	 * @see Town#research.researchhand
	 */
	public static void draw(Town t) {
		if (t.ishostile()) {
			computerdraw(t);
		} else {
			humandraw(t);
		}
	}

	private static void computerdraw(Town t) {
		if (t.research.hand[0] == null && t.size < MAXGROWTH) { // grow
			t.research.hand[0] = new Grow(t);
		}
		if (t.research.hand[1] == null) { // build lair
			t.research.hand[1] = pick(getrealmmonsters(t), t, 1);
		}
		ArrayList<RecruitOption> lairs = new ArrayList<RecruitOption>(t.lairs);
		for (int i = 2; i < t.research.hand.length; i++) {// recruit
			if (t.research.hand[i] == null) {
				t.research.hand[i] = pick(lairs, t, 1);
			}
		}
	}

	protected static void humandraw(Town t) {
		List<Upgrade> otherupgrades = new ArrayList<Upgrade>();
		List<Upgrade> myupgrades = new ArrayList<Upgrade>();
		List<Item> myitems = new ArrayList<Item>();
		List<Item> otheritems = new ArrayList<Item>();
		gatherupgrades(t, otherupgrades, myupgrades, myitems, otheritems);
		if (t.research.hand[0] == null && t.size < MAXGROWTH) {
			t.research.hand[0] = new Grow(t);
		}
		if (t.research.hand[1] == null && t.upgrades.size() < 20) {
			t.research.hand[1] = pick(myupgrades, t, 1);
		}
		if (t.research.hand[2] == null && t.upgrades.size() < 20) {
			t.research.hand[2] = pick(otherupgrades, t, 2);
		}
		if (t.research.hand[3] == null && t.items.size() < 20) {
			t.research.hand[3] = pick(myitems, t, 1);
		}
		if (t.research.hand[4] == null && t.items.size() < 20) {
			t.research.hand[4] = pick(otheritems, t, 2);
		}
		if (t.research.hand[5] == null && t.lairs.size() < 20) {
			t.research.hand[5] = pick(getrealmmonsters(t), t, 1);
		}
		if (t.research.hand[6] == null) {
			t.research.hand[6] =
					SPECIALCARDS[RPG.r(0, SPECIALCARDS.length - 1)];
		}
		if (t.research.hand[7] == null) {
			Accommodations a = t.upgradeinn();
			if (a != t.lodging) {
				t.research.hand[7] = new AccommodationResearch(a);
			}
		}
		if (t.research.hand[8] == null) {
			Transport transport = t.upgradetransport();
			if (transport != t.transport) {
				t.research.hand[8] = new TransportResearch(transport);
			}
		}
	}

	protected static ArrayList<Monster> getrealmmonsters(WorldActor t) {
		ArrayList<Monster> monsters = new ArrayList<Monster>();
		String[] terrains = Javelin.terrains(Javelin.terrain(t.x, t.y));
		for (Monster m : Javelin.ALLMONSTERS) {
			for (String terrain : terrains) {
				if (m.terrains.contains(terrain)) {
					monsters.add(m);
					break;
				}
			}
		}
		return monsters;
	}

	protected static void gatherupgrades(WorldActor t,
			List<Upgrade> otherupgrades, List<Upgrade> myupgrades,
			List<Item> myitems, List<Item> otheritems) {
		UpgradeHandler.singleton.gather();
		for (Realm r : Realm.values()) {
			List<Upgrade> upgrades = UpgradeHandler.singleton.getupgrades(r);
			ItemSelection items = Item.getselection(r);
			if (r == t.realm) {
				myupgrades.addAll(upgrades);
				myitems.addAll(items);
			} else {
				otherupgrades.addAll(upgrades);
				otheritems.addAll(items);
			}
		}
	}

	private static Research pick(List<? extends Object> list, Town t,
			int costfactor) {
		Collections.shuffle(list);
		for (Object o : list) {
			Research b;
			if (o instanceof Upgrade) {
				b = new UpgradeResearch((Upgrade) o, costfactor);
			} else if (o instanceof Item) {
				b = new ItemResearch((Item) o, costfactor);
			} else if (o instanceof Monster) {
				b = new LairResearch((Monster) o);
			} else if (o instanceof RecruitOption) {
				RecruitOption ro = (RecruitOption) o;
				b = new Recruit(ro.m);
			} else {
				throw new RuntimeException("#unknownCardType " + o);
			}
			if (!b.isrepeated(t) && !hasinhand(b, t)) {
				return b;
			}
		}
		return null;
	}

	/**
	 * Needed so the computer draw won't draw the same lair twice.
	 */
	private static boolean hasinhand(Research b, Town t) {
		for (Research r : t.research.hand) {
			if (r != null && b.equals(r)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		Research r = (Research) obj;
		return name.equals(r.name);
	}

	public abstract boolean isrepeated(Town t);

	public void finish(Town town, ResearchScreen s) {
		town.labor -= price;
		apply(town, s);
		for (int i = 0; i < town.research.hand.length; i++) {
			if (town.research.hand[i] == this) {
				town.research.hand[i] = null;
				Research.draw(town);
				return;
			}
		}
		throw new RuntimeException("#noDiscard " + this);
	}
}
