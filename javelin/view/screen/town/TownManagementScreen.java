package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Labor;
import javelin.view.screen.Option;
import javelin.view.screen.town.option.ScreenOption;

public class TownManagementScreen extends ScreenOption {
	class LaborOption extends Option {
		Labor l;

		public LaborOption(Labor l, char key) {
			super(l.name, l.cost, key);
			this.l = l;
		}
	}

	class TownManagement extends SelectScreen {
		public TownManagement(Town t) {
			super("Town management:\n\n"
					+ "A town will auto-manage itself as time goes by but this screen enables you to take control over the process.\n"
					+ printcurrent(t.governor.getqueue()) + "\nAvailable projects:", t);
		}

		@Override
		public String getCurrency() {
			return null;
		}

		@Override
		public String printpriceinfo(Option o) {
			LaborOption l = o instanceof LaborOption ? ((LaborOption) o) : null;
			return l == null ? "" : "(" + l.l.cost + " labor)";
		}

		@Override
		public String printInfo() {
			String output = "";
			// output += "Labor: " + Math.round(Math.floor(town.labor));
			if (town.ishostile() && Javelin.DEBUG) {
				output += "    Queue: " + town.governor.getqueue() + "\n";
				output += "    Garrison: " + town.garrison + "\n";
				output += "    EL: " + ChallengeRatingCalculator.calculateel(town.garrison);
				// } else {
				// output = printcurrent(town.governor.getqueue());
			}
			output += "\nSize: " + town.getranktitle() + " (" + town.size + ")";
			return output;
		}

		@Override
		public boolean select(Option o) {
			LaborOption lo = (LaborOption) o;
			town.governor.start(lo.l);
			return false;
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Labor> hand = town.governor.gethand();
			ArrayList<Option> labors = new ArrayList<Option>();
			if (!hand.isEmpty()) {
				for (int i = 0; i < hand.size(); i++) {
					Labor l = hand.get(i);
					labors.add(new LaborOption(l, Character.forDigit(i, 10)));
				}
			}
			return labors;
		}

		@Override
		public void printoptions(List<Option> options) {
			if (town.governor.gethand().isEmpty()) {
				text += "  (no labor projects available right now)";
			} else {
				super.printoptions(options);
			}
		}
	}

	public TownManagementScreen(Town town) {
		super("Manage town", town, 'm');
	}

	@Override
	public SelectScreen show() {
		return new TownManagement(t);
	}

	static public String printcurrent(ArrayList<Labor> queue) {
		String output = "\nCurrent projects:\n\n";
		for (Labor l : queue) {
			output += "  " + l.name + " (" + l.progress() + ")\n";
		}
		return output;
	}
}
