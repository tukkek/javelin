package javelin.model.world.town.research;

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
import javelin.model.world.town.Town;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.ResearchScreenOption.ResearchScreen;
import tyrant.mikera.engine.RPG;

/**
 * Represents a way in which a {@link Town} can be improved.
 * 
 * @see Town#researchhand
 * @author alex
 */
public abstract class Research extends Option {
	private static final Research[] SPECIALCARDS =
			new Research[] { new Discard() };

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
	 * @see Town#researchhand
	 */
	public static void draw(Town t) {
		UpgradeHandler.singleton.gather();
		List<Upgrade> otherupgrades = new ArrayList<Upgrade>();
		List<Upgrade> myupgrades = new ArrayList<Upgrade>();
		List<Item> myitems = new ArrayList<Item>();
		List<Item> otheritems = new ArrayList<Item>();
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
		if (t.researchhand[0] == null) {
			t.researchhand[0] = new Grow(t);
		}
		if (t.researchhand[1] == null) {
			t.researchhand[1] = pick(myupgrades, t, 1);
		}
		if (t.researchhand[2] == null) {
			t.researchhand[2] = pick(otherupgrades, t, 2);
		}
		if (t.researchhand[3] == null) {
			t.researchhand[3] = pick(myitems, t, 1);
		}
		if (t.researchhand[4] == null) {
			t.researchhand[4] = pick(otheritems, t, 2);
		}
		if (t.researchhand[5] == null) {
			String[] terrains = Javelin.terrains(Javelin.terrain(t.x, t.y));
			ArrayList<Monster> monster = new ArrayList<Monster>();
			for (Monster m : Javelin.ALLMONSTERS) {
				for (String terrain : terrains) {
					if (m.terrains.contains(terrain)) {
						monster.add(m);
						break;
					}
				}
			}
			t.researchhand[5] = pick(monster, t, 1);
		}
		if (t.researchhand[6] == null) {
			t.researchhand[6] = SPECIALCARDS[RPG.r(0, SPECIALCARDS.length - 1)];
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
			} else {
				b = new LairResearch((Monster) o);
			}
			if (!b.isrepeated(t)) {
				return b;
			}
		}
		return null;
	}

	protected abstract boolean isrepeated(Town t);

	public void finish(Town town, ResearchScreen s) {
		town.labor -= price;
		apply(town, s);
		for (int i = 0; i < town.researchhand.length; i++) {
			if (town.researchhand[i] == this) {
				town.researchhand[i] = null;
				Research.draw(town);
				return;
			}
		}
		throw new RuntimeException("#noDiscard " + this);
	}
}
