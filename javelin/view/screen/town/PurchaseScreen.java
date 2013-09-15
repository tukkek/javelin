package javelin.view.screen.town;

import javelin.Javelin;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.view.screen.town.option.Option;

public abstract class PurchaseScreen extends SelectScreen {

	protected boolean bought;

	public PurchaseScreen(final String s, final Town t) {
		super(s, t);
	}

	@Override
	boolean select(final Option o) {
		bought = Squad.active.gold >= o.price;
		if (bought) {
			Squad.active.gold -= o.price;
		} else {
			text += "\nNot enough gold.";
			Javelin.app.switchScreen(this);
		}
		return bought;
	}

	@Override
	String printInfo() {
		return "Your squad has $" + formatcost(Squad.active.gold);
	}

	@Override
	public String getCurrency() {
		return "$";
	}

	@Override
	void roundcost(final Option o) {
		o.price = Math.round(o.price);
	}
}
