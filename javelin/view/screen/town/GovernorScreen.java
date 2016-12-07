package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.action.world.Guide;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Labor;
import javelin.view.screen.Option;
import javelin.view.screen.town.option.ScreenOption;

public class GovernorScreen extends ScreenOption {
	private static final String TEMPLATE = "Town management:\n\n"//
			+ "A town will auto-manage itself but this screen enables you to control the process.\n"//
			+ "Press h to see the description for each labor.\n" //
			+ "%s"//
			+ "\nAvailable projects:";

	class LaborOption extends Option {
		Labor l;

		public LaborOption(Labor l) {
			super(l.name, l.cost);
			this.l = l;
		}
	}

	class TownManagement extends SelectScreen {
		public TownManagement(Town t) {
			super(String.format(TEMPLATE, printcurrent(t.governor.getqueue())), t);
		}

		@Override
		public String getCurrency() {
			return null;
		}

		@Override
		public String printpriceinfo(Option o) {
			LaborOption l = o instanceof LaborOption ? ((LaborOption) o) : null;
			return l == null ? "" : " (" + l.l.cost + " labor)";
		}

		@Override
		public String printInfo() {
			String output = "";
			// output += "Labor: " + Math.round(Math.floor(town.labor));
			if (town.ishostile() && Javelin.DEBUG) {
				output += "    Queue: " + town.governor.getqueue() + "\n";
				output += "    Garrison: " + town.garrison + "\n";
				output += "    EL: " + ChallengeRatingCalculator.calculateel(town.garrison);
			}
			output += "\nSize: " + town.getranktitle() + " (" + town.population + ")";
			return output;
		}

		@Override
		public boolean select(Option o) {
			LaborOption lo = (LaborOption) o;
			town.governor.start(lo.l);
			if (lo.l.cost == 0) {
				stayopen = false;
			}
			return true;
		}

		@Override
		public List<Option> getoptions() {
			ArrayList<Labor> hand = town.governor.gethand();
			ArrayList<Option> labors = new ArrayList<Option>();
			if (!hand.isEmpty()) {
				for (int i = 0; i < hand.size(); i++) {
					Labor l = hand.get(i);
					labors.add(new LaborOption(l));
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

		@Override
		protected boolean select(char feedback, List<Option> options) {
			if (feedback == 'h') {
				Guide.DISTRICT.perform();
				return true;
			}
			return super.select(feedback, options);
		}
	}

	public GovernorScreen(Town town) {
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
