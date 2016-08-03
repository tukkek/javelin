package javelin.view.screen.town;

import javelin.Javelin;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;

/**
 * Any screen in which the player can spend money tu buy something.
 * 
 * @author alex
 */
public abstract class PurchaseScreen extends SelectScreen {
	/** <code>true</code> if transaction was finalized. */
	protected boolean bought;

	/** Constructor. See {@link SelectScreen#SelectScreen(String, Town)}. */
	public PurchaseScreen(final String s, final Town t) {
		super(s, t);
	}

	@Override
	public boolean select(final Option o) {
		bought = canbuy(o);
		if (bought) {
			spend(o);
			return true;
		}
		text += "\nToo expensive!";
		Javelin.app.switchScreen(this);
		return false;
	}

	/**
	 * @param o
	 *            Make the payment for this selection.
	 */
	protected void spend(final Option o) {
		Squad.active.gold -= o.price;
	}

	/**
	 * @return <code>true</code> if can pay for the current {@link Option}.
	 */
	protected boolean canbuy(final Option o) {
		return Squad.active.gold >= o.price;
	}

	@Override
	public String printInfo() {
		return "Your squad has $" + formatcost(Squad.active.gold);
	}

	@Override
	public String getCurrency() {
		return "$";
	}

	@Override
	public void roundcost(final Option o) {
		o.price = Math.round(o.price);
	}
}
