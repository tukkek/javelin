package javelin.model.world.location.town.diplomacy.mandate;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;

/**
 * Adds gold to a {@link Squad} standing in the respective {@link District}.
 *
 * @author alex
 */
public class RequestGold extends Mandate{
	int gold;

	/** Reflection constructor. */
	public RequestGold(Town t){
		super(t);
	}

	@Override
	public void define(){
		var p=town.population;
		gold=Javelin.round(RewardCalculator.getgold(p-1,p+1));
		super.define();
	}

	@Override
	public boolean validate(){
		return gold>0;
	}

	@Override
	public String getname(){
		return "Request gold ($"+Javelin.format(gold)+")";
	}

	@Override
	public void act(){
		Squad.active.gold+=gold;
		var m="Party receives $%s!";
		Javelin.message(String.format(m,Javelin.format(gold)),true);
	}
}
