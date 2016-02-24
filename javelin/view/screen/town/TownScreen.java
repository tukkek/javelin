package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.world.QueueItem;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.model.world.TownQueue;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.RecruitScreenOption;
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
	static final Option INN = new Option("Sleep at lodge", 0);
	static final Option WAIT = new Option("Await next order", 0);
	static final Option CANCELUPGRADES = new Option("Cancel upgrades", 0);

	private Option week =
			new Option("Sleep at lodge (1 week)", 7 * .5 * Squad.active.size());
	private final Option hospital =
			new Option("Recover in hospital", Squad.active.size() * .7);

	public TownScreen(final Town t) {
		super("Welcome to " + t.name + "!", t);
		sortoptions = false;
		show();
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
			for (QueueItem member : town.training.queue) {
				town.completetraining(member);
			}
			town.training.queue.clear();
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
		list.add(new RecruitScreenOption("Recruit", town));
		list.add(new ShopScreenOption("Shop", town));
		list.add(new TransportScreenOption("Transport", town));
		list.add(INN);
		list.add(hospital);
		list.add(week);
		if (hospital.price < 1) {
			hospital.price = 1;
		}
		list.add(new UpgradingScreenOption("Upgrade", town));
		boolean istraining = !town.training.queue.isEmpty();
		if (istraining || !town.crafting.queue.isEmpty()) {
			list.add(WAIT);
			if (istraining) {
				list.add(CANCELUPGRADES);
			}
		}
		if (town.ishosting()) {
			list.add(new TournamentScreenOption("Tournament", town));
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
		return output;
	}

	private String showqueue(TownQueue queue, String output) {
		if (queue.done()) {
			return "";
		}
		output += ":\n";
		for (QueueItem i : queue.queue) {
			output += "  - " + i.payload[0] + ", time left: ";
			long hoursleft = i.completionat - Squad.active.hourselapsed;
			output += hoursleft < 24 ? hoursleft + " hour(s)"
					: Math.round(Math.round(hoursleft / 24f)) + " day(s)";
			output += "\n";
		}
		return output.substring(0, output.length() - 1);
	}
}
