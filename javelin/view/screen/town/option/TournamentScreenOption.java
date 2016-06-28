package javelin.view.screen.town.option;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.tournament.Exhibition;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;
import javelin.view.screen.town.PurchaseScreen;
import javelin.view.screen.town.SelectScreen;

/**
 * Lets a player select an {@link Exhibition} to join.
 * 
 * @author alex
 */
public class TournamentScreenOption extends ScreenOption {
	class EventOption extends Option {
		Exhibition event;

		public EventOption(String name, double d, Exhibition eventp) {
			super(name, d);
			event = eventp;
		}
	}

	/**
	 * Prices the match at half the reward.
	 */
	public TournamentScreenOption(String name, Town town, char c) {
		super(name, town, c);
		price = RewardCalculator.receivegold(Squad.active.members) / 2;
	}

	@Override
	public SelectScreen show() {
		return new PurchaseScreen("Join: ", t) {
			@Override
			public boolean select(Option o) {
				if (!super.select(o)) {
					return false;
				}
				Exhibition e = ((EventOption) o).event;
				town.events.remove(e);
				e.start();
				return true;
			}

			@Override
			public String printInfo() {
				return "";
			}

			@Override
			public List<Option> getoptions() {
				ArrayList<Option> options = new ArrayList<Option>();
				for (Exhibition e : town.events) {
					options.add(new EventOption(e.name, price, e));
				}
				return options;
			}
		};

	}
}
