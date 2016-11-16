package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Research;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.town.option.TournamentScreenOption;
import javelin.view.screen.town.option.UnloadScreenOption;

/**
 * Shown when a {@link Squad} enters a {@link Town}.
 * 
 * @author alex
 */
public class TownScreen extends PurchaseScreen {
	final static boolean DEBUGMANAGEMENT = false;

	// static final Option AWAIT = new Option("Await next order", 0, 'a');
	// static final Option CANCELUPGRADES = new Option("Cancel upgrades", 0,
	// 'c');
	static final Option RENAME = new Option("Rename town", 0, 'r');
	static final Option DETACHWORKER = new Option("Detach worker", 0, 'd');
	static final Option OPENCARD = new Option(
			"Open one new labor option (1 labor)", 0, 'o');
	static final Option REDRAWCARDS = new Option(
			"Redraw all labor options (x labor)", 0, 'a');

	private static final Option MANAGE = new Option("", 0, 'm');

	private Squad entering;

	/** Constructor. */
	public TownScreen(final Town t) {
		super(title(t), t);
		entering = Squad.active;
		show();
	}

	static String title(final WorldActor t) {
		return "Welcome to " + t + "!";
	}

	@Override
	public boolean select(final Option o) {
		if (o instanceof ScreenOption) {
			ScreenOption screen = (ScreenOption) o;
			screen.show().show();
			return true;
		}
		if (!super.select(o)) {
			return false;
		}
		// if (o == CANCELUPGRADES) {
		// for (Order member : town.training.queue) {
		// TrainingOrder to = (TrainingOrder) member;
		// TownUpgradingScreen.completetraining(to, town, to.untrained);
		// }
		// town.training.queue.clear();
		// return true;
		// }
		if (o == RENAME) {
			town.rename();
			title = title(town) + "\n\n";
			return true;
		}
		if (o == MANAGE) {
			town.governor.automanage = !town.governor.automanage;
			town.governor.queue.clear();
			return true;
		}
		if (o instanceof SettleOption) {
			return SettleOption.retire(town);
		}
		if (o instanceof UnloadScreenOption) {
			town.labor += Squad.active.resources;
			Squad.active.resources = 0;
			return true;
		}
		// if (o instanceof RestOption) {
		// RestOption ro = (RestOption) o;
		// Town.rest(ro.periods, ro.hours, town.lodging);
		// } else if (o == AWAIT) {
		// Long training = town.training.next();
		// Long crafting = town.crafting.next();
		// long hours =
		// Math.min(training == null ? Integer.MAX_VALUE : training,
		// crafting == null ? Integer.MAX_VALUE : crafting)
		// - Squad.active.hourselapsed;
		// Town.rest((int) Math.floor(hours / 8f), hours, town.lodging);
		// } else
		if (o == DETACHWORKER) {
			Town.getworker(town);
		}
		if (o == OPENCARD) {
			return drawone();
		}
		if (o == REDRAWCARDS) {
			return redrawall();
		}
		stayopen = false;
		return true;
	}

	boolean drawone() {
		if (town.labor < 1) {
			text += "\nToo expensive...";
			return false;
		}
		if (!town.governor.draw()) {
			text += "\nNo labor option available...";
			return false;
		}
		town.labor -= 1;
		return true;
	}

	boolean redrawall() {
		int cost = town.governor.gethandsize();
		if (town.labor < cost) {
			text += "\nToo expensive...";
			return false;
		}
		if (town.governor.redraw() == 0) {
			text += "\nNo labor options available...";
			return false;
		}
		town.labor -= cost;
		return true;
	}

	@Override
	public List<Option> getoptions() {
		final ArrayList<Option> list = new ArrayList<Option>();
		if (Squad.active != entering) {
			return list;
		}
		if (Squad.active.resources > 0) {
			list.add(new UnloadScreenOption("Unload " + Squad.active.resources
					+ " resources into town", 0, 'U'));
		}
		// list.add(new RecruitScreenOption("Draft", town, 'd'));
		// list.add(new ShopScreenOption("Shop", town, 's'));
		// if (town.transport != null) {
		// list.add(new TransportScreenOption("Rent transport", town, 't'));
		// }
		// list.add(town.lodging.getrestoption());
		// list.add(town.lodging.getweekrestoption());
		// list.add(new UpgradingScreenOption("Upgrade", town, 'u'));
		// boolean istraining = !town.training.queue.isEmpty();
		// if (istraining || !town.crafting.queue.isEmpty()) {
		// list.add(AWAIT);
		// if (istraining) {
		// list.add(CANCELUPGRADES);
		// }
		// }
		if (town.ishosting()) {
			list.add(new TournamentScreenOption("Enter tournament", town, 'T'));
		}
		MANAGE.name =
				"Manage town ("
						+ (town.governor.automanage ? "automatic" : "manual")
						+ ")";
		if (town.size > 1) {
			list.add(DETACHWORKER);
		}
		list.add(new SettleOption());
		list.add(MANAGE);
		if (!town.governor.automanage) {
			// list.add(new ResearchScreenOption(town));
			if (!town.governor.isfull()) {
				list.add(OPENCARD);
			}
			list.add(REDRAWCARDS);
			REDRAWCARDS.name =
					"Redraw all labor options (" + town.governor.gethandsize()
							+ " labor)";
			list.add(RENAME);
		}
		return list;
	}

	@Override
	public String printpriceinfo(Option o) {
		return o.price > 0 ? super.printpriceinfo(o) : "";
	}

	@Override
	public void onexit() {
		Javelin.app.switchScreen(BattleScreen.active);
	}

	@Override
	public String printInfo() {
		String output =
				"Your squad has $"
						+ PurchaseScreen.formatcost(Squad.active.gold);
		// if (!town.crafting.done()) {
		// output += "\n\n" + showqueue(town.crafting, "Crafting");
		// }
		// if (!town.training.done()) {
		// output += "\n\n" + showqueue(town.training, "Training");
		// }
		if (!town.governor.automanage || DEBUGMANAGEMENT) {
			output +=
					"\n\nSize: " + town.getranktitle() + " (" + town.size
							+ ")\n";
			output += "Labor: " + Math.round(Math.floor(town.labor));
			if (town.ishostile() && Javelin.DEBUG) {
				output += "    Queue: " + town.governor.queue + "\n";
				output += "    Garrison: " + town.garrison + "\n";
				output +=
						"    EL: "
								+ ChallengeRatingCalculator
										.calculateel(town.garrison);
			} else if (!town.governor.queue.isEmpty()) {
				output += "\nLabor queue: ";
				if (town.governor.queue.isEmpty() && DEBUGMANAGEMENT) {
					output += town.governor.queue.get(0);
				} else {
					output += town.governor.printqueue();
				}
			}
			output += "\n\nAvailable labor:\n" + town.governor.printhand();
		}
		return output;
	}

	private String showqueue(OrderQueue queue, String output) {
		if (queue.done()) {
			return "";
		}
		output += ":\n";
		for (Order i : queue.queue) {
			output += "  - " + i.name + ", time left: ";
			long hoursleft = i.completionat - Squad.active.hourselapsed;
			output +=
					hoursleft < 24 ? hoursleft + " hour(s)" : Math.round(Math
							.round(hoursleft / 24f)) + " day(s)";
			output += "\n";
		}
		return output.substring(0, output.length() - 1);
	}

	// @Override
	// protected Comparator<Option> sort() {
	// // return new Comparator<Option>() {
	// // @Override
	// // public int compare(Option o1, Option o2) {
	// // return o1.key.compareTo(o2.key);
	// // }
	// // };
	// return null;
	// }

	@Override
	protected void sort(List<Option> options) {
		// super.sort(options);
		// ArrayList<Option> uppercase = new ArrayList<Option>();
		// for (Option o : new ArrayList<Option>(options)) {
		// if (Character.isUpperCase(o.key)) {
		// options.remove(o);
		// uppercase.add(o);
		// }
		// }
		// for (Option o : uppercase) {
		// options.add(o);
		// }
	}

	// @Override
	// protected Comparator<Option> sort() {
	// // TODO Auto-generated method stub
	// return super.sort();
	// }
	@Override
	protected boolean select(char feedback, List<Option> options) {
		if (!town.governor.automanage && Character.isDigit(feedback)) {
			return selectlabor(Integer.parseInt(Character.toString(feedback)) - 1);
		}
		return super.select(feedback, options);
	}

	boolean selectlabor(int i) {
		if (i < 0 || i >= town.governor.hand.size()) {
			return false;
		}
		Research r = town.governor.hand.get(i);
		if (r.cost > town.labor) {
			text += "\nToo expensive...";
			return false;
		}
		town.labor -= r.cost;
		r.play(town);
		town.governor.hand.remove(r);
		return true;
	}
}
