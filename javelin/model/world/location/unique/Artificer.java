package javelin.model.world.location.unique;

import java.util.List;

import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.shopping.ArtificerScreen;
import javelin.view.screen.town.PurchaseOption;
import tyrant.mikera.engine.RPG;

/**
 * Allows a player to forge and sell artifacts.
 * 
 * @author alex
 */
public class Artificer extends UniqueLocation {
	private static final String DESCRIPTION = "The artificer";

	static boolean DEBUG = false;

	/**
	 * {@link Artifact}s this artificer can craft. This generically represents
	 * the base components the Artificer has at the moment to create this
	 * selection of magic items. Will randomly replace an item once per month,
	 * representing not only a possible sell but also getting new alchemical
	 * components, old ones spoiling, etc.
	 */
	public ItemSelection selection = new ItemSelection();
	Item crafting = null;
	long completeat = Integer.MIN_VALUE;

	/** Constructor. */
	public Artificer() {
		super(DESCRIPTION, DESCRIPTION, 11, 15);
		while (selection.size() < 9) {
			additem();
		}
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
			crafting.grab();
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
	public boolean hascrafted() {
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

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}
}
