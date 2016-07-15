package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.old.Game;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Research;
import javelin.view.screen.Option;

/**
 * Manually manages {@link Town#research}.
 * 
 * @see Town#automanage
 * @author alex
 */
public class ResearchScreen extends PurchaseScreen {

	private Town t;

	public ResearchScreen(String name, Town t) {
		super("Reseach new options:", t);
		this.t = t;
		/*
		 * TODO initial towns are drawing upgrades before stashing other options
		 * so some isrepeated options are being added nonetheless. This
		 * mitigates by checking again on Screen opening.
		 */
		for (int i = 0; i < t.research.hand.length; i++) {
			Research o = t.research.hand[i];
			if (o != null && o.isrepeated(t)) {
				t.research.hand[i] = null;
			}
		}
		Research.draw(t);
	}

	@Override
	public String printpriceinfo(Option o) {
		return ", " + Math.round(Math.ceil(o.price)) + " labor";
	}

	@Override
	public String printInfo() {
		return "Current labor: " + Math.round(Math.floor(t.labor)) + "\n"
				+ "Queue: " + town.research.queue;
	}

	@Override
	public boolean select(Option o) {
		Research r = (Research) o;
		boolean canpay = Math.floor(town.labor) >= Math.ceil(o.price);
		if (r.immediate) {
			if (canpay) {
				r.finish(town, this);
			} else {
				print("Not enough labor!");
				Game.getInput();
			}
			return true;
		}
		if (town.research.queue.isEmpty() && canpay) {
			r.finish(town, null);
		} else if (!town.research.queue.contains(r)) {
			town.research.queue.add(r);
		}
		return true;
	}

	@Override
	public List<Option> getoptions() {
		ArrayList<Option> options = new ArrayList<Option>();
		for (Option o : town.research.hand) {
			if (o != null) {
				options.add(o);
			}
		}
		return options;
	}

	@Override
	protected boolean canbuy(Option o) {
		return true;
	}

	@Override
	protected void spend(Option o) {
		// see select
	}

	@Override
	protected void sort(List<Option> options) {
		// don't sort
	}
}
