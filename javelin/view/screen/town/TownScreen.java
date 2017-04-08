package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.location.fortification.Fortification;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.OrderQueue;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.town.option.TournamentScreenOption;

/**
 * Shown when a {@link Squad} enters a {@link Town}.
 *
 * @author alex
 */
public class TownScreen extends PurchaseScreen {
	private static final Option UNLOAD = new Option("", 0, 'U');
	private static final Option SETTLE = new Option("Settle worker", 0, 's');
	final static boolean DEBUGMANAGEMENT = false;
	static final Option RENAME = new Option("Rename town", 0, 'r');
	private static final Option PILLAGE = new Option("Pillage", 0, 'P');
	// private static final Option MANAGE = new ScreenOption("Manage town", 0,
	// 'm');
	private Actor entering;

	/** Constructor. */
	public TownScreen(final Town t) {
		super(title(t), t);
		entering = Squad.active;
		show();
	}

	static String title(final Actor t) {
		return "Welcome to " + t + "!";
	}

	@Override
	public boolean select(final Option o) {
		if (o instanceof ScreenOption) {
			SelectScreen screen = ((ScreenOption) o).show();
			screen.show();
			if (screen.forceclose) {
				stayopen = false;
			}
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
		// if (o == MANAGE) {
		// town.governor.automanage = !town.governor.automanage;
		// town.governor.queue.clear();
		// return true;
		// }
		if (o == SETTLE) {
			return retire(town);
		}
		if (o == UNLOAD) {
			town.governor.work(Squad.active.resources);
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
		// if (o == DETACHWORKER) {
		// Town.getworker(town);
		// }
		// if (o == OPENCARD) {
		// return drawone();
		// }
		// if (o == REDRAWCARDS) {
		// return redrawall();
		// }
		if (o == PILLAGE) {
			Squad.active.gold += Fortification.getspoils(town.population);
			town.remove();
			stayopen = false;
			return true;
		}
		stayopen = false;
		return true;
	}

	private boolean retire(Town town) {
		List<Combatant> retirees = new ArrayList<Combatant>();
		for (Combatant c : Squad.active.members) {
			if (!c.mercenary) {
				retirees.add(c);
			}
		}
		if (retirees.isEmpty()) {
			return false;
		}
		int choice = Javelin.choose(
				"Which member should retire and become local labor?", retirees,
				true, false);
		if (choice < 0) {
			return false;
		}
		Squad.active.remove(retirees.get(choice));
		town.population += 1;
		return true;
	}

	//
	// boolean drawone() {
	// if (town.labor < 1) {
	// text += "\nToo expensive...";
	// return false;
	// }
	// if (!town.governor.draw()) {
	// text += "\nNo labor option available...";
	// return false;
	// }
	// town.labor -= 1;
	// return true;
	// }

	// boolean redrawall() {
	// int cost = town.governor.gethandsize();
	// if (town.labor < cost) {
	// text += "\nToo expensive...";
	// return false;
	// }
	// if (town.governor.redraw() == 0) {
	// text += "\nNo labor options available...";
	// return false;
	// }
	// town.labor -= cost;
	// return true;
	// }

	@Override
	public List<Option> getoptions() {
		final ArrayList<Option> list = new ArrayList<Option>();
		if (Squad.active != entering) {
			return list;
		}
		if (Squad.active.resources > 0) {
			UNLOAD.name = "Unload " + Squad.active.resources
					+ " resources into town";
			list.add(UNLOAD);
		}
		if (town.ishosting()) {
			list.add(new TournamentScreenOption("Enter tournament", town, 't'));
		}
		list.add(SETTLE);
		list.add(new GovernorScreen(town));
		if (town.getrank().rank < Rank.CITY.rank) {
			list.add(RENAME);
			PILLAGE.name = "Pillage ($" + SelectScreen.formatcost(
					Fortification.getspoils(town.population - 1)) + ")";
			list.add(PILLAGE);
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
	public String printinfo() {
		// if (!town.crafting.done()) {
		// output += "\n\n" + showqueue(town.crafting, "Crafting");
		// }
		// if (!town.training.done()) {
		// output += "\n\n" + showqueue(town.training, "Training");
		// }
		// if (!town.governor.automanage || DEBUGMANAGEMENT) {
		// }
		return "Your squad has $" + SelectScreen.formatcost(Squad.active.gold);
	}

	private String showqueue(OrderQueue queue, String output) {
		if (queue.reportalldone()) {
			return "";
		}
		output += ":\n";
		for (Order i : queue.queue) {
			output += "  - " + i.name + ", time left: ";
			long hoursleft = i.completionat - Squad.active.hourselapsed;
			output += hoursleft < 24 ? hoursleft + " hour(s)"
					: Math.round(Math.round(hoursleft / 24f)) + " day(s)";
			output += "\n";
		}
		return output.substring(0, output.length() - 1);
	}

	@Override
	protected Comparator<Option> sort() {
		return new Comparator<Option>() {
			@Override
			public int compare(Option o1, Option o2) {
				return o1.key.compareTo(o2.key);
			}
		};
	}

	// @Override
	// protected void sort(List<Option> options) {
	// // super.sort(options);
	// // ArrayList<Option> uppercase = new ArrayList<Option>();
	// // for (Option o : new ArrayList<Option>(options)) {
	// // if (Character.isUpperCase(o.key)) {
	// // options.remove(o);
	// // uppercase.add(o);
	// // }
	// // }
	// // for (Option o : uppercase) {
	// // options.add(o);
	// // }
	// }

	// @Override
	// protected Comparator<Option> sort() {
	// // TODO Auto-generated method stub
	// return super.sort();
	// }
	@Override
	protected boolean select(char feedback, List<Option> options) {
		// if (!town.governor.automanage && Character.isDigit(feedback)) {
		// return selectlabor(Integer.parseInt(Character.toString(feedback)) -
		// 1);
		// }
		return super.select(feedback, options);
	}

	// boolean selectlabor(int i) {
	// if (i < 0 || i >= town.governor.hand.size()) {
	// return false;
	// }
	// Labor r = town.governor.hand.get(i);
	// if (r.cost > town.labor) {
	// text += "\nToo expensive...";
	// return false;
	// }
	// town.labor -= r.cost;
	// r.done(town);
	// town.governor.hand.remove(r);
	// return true;
	// }

}
