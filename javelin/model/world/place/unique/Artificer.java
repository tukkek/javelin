package javelin.model.world.place.unique;

import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.artifact.Artifact;
import javelin.model.world.Squad;
import javelin.model.world.place.town.Town;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.shopping.ArtificerScreen;
import javelin.view.screen.town.PurchaseOption;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Allows a player to forge and sell artifacts.
 * 
 * @author alex
 */
public class Artificer extends UniqueLocation {
	static boolean DEBUG = false;

	/**
	 * TODO unify Artifact and Item {@link #equals(Object)}.
	 * 
	 * @author alex
	 */
	public class Selection extends ItemSelection {
		@Override
		public boolean add(Item add) {
			for (Item a : this) {
				if (a.getClass().equals(add.getClass())) {
					return false;
				}
			}
			super.add(add);
			return true;
		}

	}

	/**
	 * {@link Artifact}s this artificer can craft. This generically represents
	 * the base components the Artificer has at the moment to create this
	 * selection of magic items. Will randomly replace an item once per month,
	 * representing not only a possible sell but also getting new alchemical
	 * components, old ones spoiling, etc.
	 */
	public Selection selection = new Selection();
	Item crafting = null;
	long completeat = Integer.MIN_VALUE;

	/** Constructor. */
	public Artificer() {
		super("The arcane university", "The arcane university", 11, 15);
		while (selection.size() < 9) {
			additem();
		}
		gossip = true;
		if (DEBUG) {
			garrison.clear();
		}
	}

	void additem() {
		while (!selection.add(RPG.pick(Item.ARTIFACT))) {
			// wait until 1 item enters
		}
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		if (crafting == null) {
			new ArtificerScreen(this).show();
			return true;
		}
		long eta = completeat - Squad.active.hourselapsed;
		if (eta <= 0) {
			Town.grab(crafting);
			crafting = null;
			completeat = Integer.MIN_VALUE;
			return true;
		}
		Game.messagepanel.clear();
		Game.message(
				"\"Come back in " + Math.round(Math.ceil(eta / 24f))
						+ " days for your " + crafting.toString().toLowerCase()
						+ ".\"\n\nPress any key to coninue...",
				null, Delay.NONE);
		Game.getInput();
		return true;
	}

	@Override
	public void turn(long time, WorldScreen world) {
		if (RPG.random() <= 1 / 30f) {
			selection.remove(RPG.pick(selection));
			additem();
		}
	}

	@Override
	public boolean iscrafting() {
		return crafting != null && Squad.active.hourselapsed >= completeat;
	}

	/**
	 * @param o
	 *            Start crafting this {@link PurchaseOption#i} .
	 */
	public void craft(PurchaseOption o) {
		selection.remove(o.i);
		crafting = o.i;
		completeat =
				Math.round(24 * o.price / 1000 + Squad.active.hourselapsed);
		additem();
	}
}
