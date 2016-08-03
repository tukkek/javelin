package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.town.Accommodations.RestOption;
import javelin.model.world.location.town.Order;
import javelin.model.world.location.town.OrderQueue;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.TrainingOrder;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.option.RecruitScreenOption;
import javelin.view.screen.town.option.ResearchScreenOption;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.town.option.ShopScreenOption;
import javelin.view.screen.town.option.TournamentScreenOption;
import javelin.view.screen.town.option.TransportScreenOption;
import javelin.view.screen.town.option.UpgradingScreenOption;
import javelin.view.screen.upgrading.TownUpgradingScreen;

/**
 * Shown when a {@link Squad} enters a {@link Town}.
 * 
 * @author alex
 */
public class TownScreen extends PurchaseScreen {
	final static boolean DEBUGMANAGEMENT = false;

	static final Option AWAIT = new Option("Await next order", 0, 'a');
	static final Option CANCELUPGRADES = new Option("Cancel upgrades", 0, 'c');
	static final Option RENAME = new Option("Rename town", 0, 'N');
	static final Option DETACHWORKER = new Option("Detach worker", 0, 'W');

	private static final Option MANAGE = new Option("", 0, 'M');

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
		if (o == CANCELUPGRADES) {
			for (Order member : town.training.queue) {
				TrainingOrder to = (TrainingOrder) member;
				TownUpgradingScreen.completetraining(to, town, to.untrained);
			}
			town.training.queue.clear();
			return true;
		}
		if (o == RENAME) {
			town.rename();
			title = title(town) + "\n\n";
			return true;
		}
		if (o == MANAGE) {
			town.automanage = !town.automanage;
			if (town.automanage) {
				town.research.queue.clear();
			} else {
				town.nexttask = null;
			}
			return true;
		}
		if (o instanceof SettleOption) {
			return SettleOption.retire(town);
		}
		if (o instanceof RestOption) {
			RestOption ro = (RestOption) o;
			Town.rest(ro.periods, ro.hours, town.lodging);
		}
		if (o == AWAIT) {
			Long training = town.training.next();
			Long crafting = town.crafting.next();
			long hours =
					Math.min(training == null ? Integer.MAX_VALUE : training,
							crafting == null ? Integer.MAX_VALUE : crafting)
							- Squad.active.hourselapsed;
			Town.rest((int) Math.floor(hours / 8f), hours, town.lodging);
		}
		if (o == DETACHWORKER) {
			Town.getworker(town);
		}
		stayopen = false;
		return true;
	}

	@Override
	public List<Option> getoptions() {
		final ArrayList<Option> list = new ArrayList<Option>();
		if (Squad.active != entering) {
			return list;
		}
		list.add(new RecruitScreenOption("Draft", town, 'd'));
		list.add(new ShopScreenOption("Shop", town, 's'));
		if (town.transport != null) {
			list.add(new TransportScreenOption("Rent transport", town, 't'));
		}
		list.add(town.lodging.getrestoption());
		list.add(town.lodging.getweekrestoption());
		list.add(new UpgradingScreenOption("Upgrade", town, 'u'));
		boolean istraining = !town.training.queue.isEmpty();
		if (istraining || !town.crafting.queue.isEmpty()) {
			list.add(AWAIT);
			if (istraining) {
				list.add(CANCELUPGRADES);
			}
		}
		if (town.ishosting()) {
			list.add(new TournamentScreenOption("Enter tournament", town, 'T'));
		}
		MANAGE.name = "Manage town ("
				+ (town.automanage ? "automatic" : "manual") + ")";
		list.add(MANAGE);
		if (!town.automanage) {
			list.add(new ResearchScreenOption(town));
			list.add(new SettleOption());
			list.add(RENAME);
			if (town.size > 1) {
				list.add(DETACHWORKER);
			}
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
		String output = "Your squad has $"
				+ PurchaseScreen.formatcost(Squad.active.gold);
		if (!town.crafting.done()) {
			output += "\n\n" + showqueue(town.crafting, "Crafting");
		}
		if (!town.training.done()) {
			output += "\n\n" + showqueue(town.training, "Training");
		}
		if (!town.automanage || DEBUGMANAGEMENT) {
			output += "\n\nManagement:\n\n";
			output += "    Size: " + town.size + "\n";
			output += "    Labor: " + Math.round(Math.floor(town.labor)) + "\n";
			if (Javelin.DEBUG && town.ishostile()) {
				output += "    Queue: " + town.nexttask + "\n";
				output += "    Garrison: " + town.garrison + "\n";
				output += "    EL: "
						+ ChallengeRatingCalculator.calculateel(town.garrison);
			} else {
				final boolean debugcomputerai =
						town.research.queue.isEmpty() && DEBUGMANAGEMENT;
				output += "    Queue: " + (debugcomputerai ? town.nexttask
						: town.research.queue);
			}
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
				return o1.name.compareTo(o2.name);
			}
		};
	}

	@Override
	protected void sort(List<Option> options) {
		super.sort(options);
		ArrayList<Option> uppercase = new ArrayList<Option>();
		for (Option o : new ArrayList<Option>(options)) {
			if (Character.isUpperCase(o.key)) {
				options.remove(o);
				uppercase.add(o);
			}
		}
		for (Option o : uppercase) {
			options.add(o);
		}
	}
}
