package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.world.Squad;
import javelin.model.world.town.Order;
import javelin.model.world.town.OrderQueue;
import javelin.model.world.town.Town;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.RecruitScreenOption;
import javelin.view.screen.town.option.ResearchScreenOption;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.town.option.ShopScreenOption;
import javelin.view.screen.town.option.TournamentScreenOption;
import javelin.view.screen.town.option.TransportScreenOption;
import javelin.view.screen.town.option.UpgradingScreenOption;

/**
 * Shown when a {@link Squad} enters a {@link Town}.
 * 
 * @author alex
 */
public class TownScreen extends PurchaseScreen {
	static final Option WAIT = new Option("Wait for next order", 0, 'w');
	static final Option CANCELUPGRADES = new Option("Cancel upgrades", 0, 'c');
	private static final Option RENAME = new Option("Rename town", 0, 'N');

	static final Option INN = new Option("Rest at lodge", 0, 'l');
	private static final Option MANAGE = new Option("", 0, 'M');
	private Option week = new Option("Rest at lodge (1 week)",
			7 * .5 * Squad.active.size(), 'W');
	private final Option hospital =
			new Option("Rest at hospital", Squad.active.size() * .7, 'h');

	public TownScreen(final Town t) {
		super(title(t), t);
		show();
	}

	protected static String title(final Town t) {
		return "Welcome to " + t.name + "!";
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
				town.completetraining(member);
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
				town.researching.clear();
			} else {
				town.nexttask = null;
			}
			return true;
		}
		rest(o);
		stayopen = false;
		return true;
	}

	public void rest(final Option o) {
		if (o == INN) {
			Town.rest(1, 8);
		} else if (o == hospital) {
			Town.rest(2, 8);
		} else if (o == week) {
			Town.rest(14, 7 * 24);
		} else if (o == WAIT) {
			Long training = town.training.next();
			Long crafting = town.crafting.next();
			long hours =
					Math.min(training == null ? Integer.MAX_VALUE : training,
							crafting == null ? Integer.MAX_VALUE : crafting)
					- Squad.active.hourselapsed;
			Town.rest((int) Math.floor(hours / 8f), hours);
		}
	}

	@Override
	public List<Option> getOptions() {
		final ArrayList<Option> list = new ArrayList<Option>();
		list.add(new RecruitScreenOption("Recruit", town, 'r'));
		list.add(new ShopScreenOption("Shop", town, 's'));
		list.add(new TransportScreenOption("Rent transport", town, 't'));
		list.add(INN);
		list.add(hospital);
		list.add(week);
		if (hospital.price < 1) {
			hospital.price = 1;
		}
		list.add(new UpgradingScreenOption("Upgrade", town, 'u'));
		boolean istraining = !town.training.queue.isEmpty();
		if (istraining || !town.crafting.queue.isEmpty()) {
			list.add(WAIT);
			if (istraining) {
				list.add(CANCELUPGRADES);
			}
		}
		if (town.ishosting()) {
			list.add(new TournamentScreenOption("Enter tournament", town, 'T'));
		}
		list.add(RENAME);
		MANAGE.name = "Manage town ("
				+ (town.automanage ? "automatic" : "manual") + ")";
		list.add(MANAGE);
		if (!town.automanage) {
			list.add(new ResearchScreenOption(town));
		}
		return list;
	}

	@Override
	public String printpriceinfo(Option o) {
		return o == INN || o.price > 0 ? super.printpriceinfo(o) : "";
	}

	@Override
	public void onexit() {
		Javelin.app.switchScreen(BattleScreen.active);
	}

	@Override
	public String printInfo() {
		String output = "Your squad has $" + Squad.active.gold;
		if (!town.crafting.done()) {
			output += "\n\n" + showqueue(town.crafting, "Crafting");
		}
		if (!town.training.done()) {
			output += "\n\n" + showqueue(town.training, "Training");
		}
		if (!town.automanage) {
			output += "\n\nManagement:\n\n";
			output += "    Size: " + town.size + "\n";
			output += "    Labor: " + Math.round(Math.floor(town.labor)) + "\n";
			output += "    Queue: " + town.researching;
		}
		return output;
	}

	private String showqueue(OrderQueue queue, String output) {
		if (queue.done()) {
			return "";
		}
		output += ":\n";
		for (Order i : queue.queue) {
			output += "  - " + i.payload[0] + ", time left: ";
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
