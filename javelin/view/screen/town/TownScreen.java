package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
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
	static final Option UNLOAD = new Option("", 0, 'U');
	static final Option SETTLE = new Option("Settle worker", 0, 's');
	static final boolean DEBUGMANAGEMENT = false;
	static final Option RENAME = new Option("Rename town", 0, 'r');
	static final Option PILLAGE = new Option("Pillage", 0, 'P');

	class Manage extends ScreenOption {
		public Manage(Town town) {
			super("Manage town", town, 'm');
		}

		@Override
		public SelectScreen getscreen() {
			return new GovernorScreen(t);
		}
	}

	/** Constructor. */
	public TownScreen(final Town t) {
		super(title(t), t);
		show();
	}

	static String title(final Actor t) {
		return "Welcome to " + t + "!";
	}

	@Override
	public boolean select(final Option o) {
		if (o instanceof ScreenOption) {
			SelectScreen screen = ((ScreenOption) o).getscreen();
			screen.show();
			if (screen.forceclose) {
				stayopen = false;
			}
			return true;
		}
		if (!super.select(o)) {
			return false;
		}
		if (o == RENAME) {
			town.rename();
			title = title(town) + "\n\n";
			return true;
		}
		if (o == SETTLE) {
			return retire(town);
		}
		if (o == UNLOAD) {
			town.governor.work(Squad.active.resources, town.getdistrict());
			Squad.active.resources = 0;
			return true;
		}
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
		List<Combatant> retirees = new ArrayList<>();
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

	@Override
	public List<Option> getoptions() {
		final ArrayList<Option> list = new ArrayList<>();
		if (World.scenario.labormodifier > 0) {
			list.add(new Manage(town));
		}
		list.add(RENAME);
		list.add(SETTLE);
		if (Squad.active.resources > 0) {
			UNLOAD.name = "Unload " + Squad.active.resources
					+ " resources into town";
			list.add(UNLOAD);
		}
		if (town.ishosting()) {
			list.add(new TournamentScreenOption("Enter tournament", town, 't'));
		}
		if (town.getrank().rank < Rank.CITY.rank) {
			int spoils = Fortification.getspoils(town.population - 1);
			PILLAGE.name = "Pillage ($" + Javelin.format(spoils) + ")";
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
		return "Your squad has $" + Javelin.format(Squad.active.gold);
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

	@Override
	protected boolean select(char feedback, List<Option> options) {
		return super.select(feedback, options);
	}
}
