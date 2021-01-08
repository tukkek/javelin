package javelin.view.screen.town.option;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.content.fight.tournament.Exhibition;
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
public class TournamentScreenOption extends ScreenOption{
	class EventOption extends Option{
		Exhibition event;

		public EventOption(String name,double d,Exhibition eventp){
			super(name,d);
			event=eventp;
		}
	}

	/**
	 * Prices the match at half the reward.
	 */
	public TournamentScreenOption(String name,Town town,char c){
		super(name,town,c);
		price=RewardCalculator.receivegold(Squad.active.members)/2;
	}

	@Override
	public SelectScreen getscreen(){
		return new PurchaseScreen("Join: ",t){
			@Override
			public boolean select(Option o){
				if(!super.select(o)) return false;
				Exhibition e=((EventOption)o).event;
				town.exhibitions.remove(e);
				e.start();
				return true;
			}

			@Override
			public String printinfo(){
				return "You currently have $"+Javelin.format(Squad.active.gold)+".";
			}

			@Override
			public List<Option> getoptions(){
				ArrayList<Option> options=new ArrayList<>();
				for(Exhibition e:town.exhibitions)
					options.add(new EventOption(e.name,price,e));
				return options;
			}
		};

	}
}
