package javelin.view.screen.town.option;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.tournament.Exhibition;
import javelin.model.world.town.Town;
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

	public TournamentScreenOption(String name, Town town) {
		super(name, town);
	}

	@Override
	public SelectScreen show() {
		return new SelectScreen("Join: ", t) {
			@Override
			public boolean select(Option o) {
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
			public List<Option> getOptions() {
				ArrayList<Option> options = new ArrayList<Option>();
				for (Exhibition e : town.events) {
					options.add(new EventOption(e.name, 0, e));
				}
				return options;
			}

			@Override
			public String getCurrency() {
				return "";
			}

			@Override
			public String printpriceinfo(Option o) {
				return "";
			}
		};

	}
}
