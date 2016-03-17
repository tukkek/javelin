package javelin.view.screen.town.option;

import java.util.ArrayList;
import java.util.List;

import javelin.model.world.town.Town;
import javelin.model.world.town.research.Research;
import javelin.view.screen.town.PurchaseScreen;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.tyrant.Game;

/**
 * Opens up a {@link ResearchScreen}.
 * 
 * @author alex
 */
public class ResearchScreenOption extends ScreenOption {

	/**
	 * Manually manages {@link Town#researching} and {@link Town#researchhand}.
	 * 
	 * @see Town#automanage
	 * @author alex
	 */
	public class ResearchScreen extends PurchaseScreen {

		public ResearchScreen(String name, Town t) {
			super("Reseach new options:", t);
		}

		@Override
		public String printpriceinfo(Option o) {
			return ", " + Math.round(Math.ceil(o.price)) + " labor";
		}

		@Override
		public String printInfo() {
			return "Current labor: " + Math.round(Math.floor(t.labor));
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
			if (town.researching.isEmpty() && canpay) {
				r.finish(town, null);
			} else if (!town.researching.contains(r)) {
				town.researching.add(r);
				print(text + "\nAdded to queue, press any key to continue...");
				Game.getInput();
			}
			return true;
		}

		@Override
		public List<Option> getOptions() {
			ArrayList<Option> options = new ArrayList<Option>();
			for (Option o : town.researchhand) {
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

	public ResearchScreenOption(Town town) {
		super("Research / add to queue", town, 'R');
	}

	@Override
	public SelectScreen show() {
		return new ResearchScreen(name, t);
	}

}
