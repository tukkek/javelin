package javelin.model.diplomacy.mandate;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.town.diplomacy.Diplomacy;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.District;
import javelin.model.world.location.town.Town;

/**
 * Adds gold to a {@link Squad} standing in the respective {@link District}.
 *
 * @author alex
 */
public class RequestGold extends Mandate{
	/** Reflection constructor. */
	public RequestGold(Town t){
		super(t);
	}

	int getgold(){
		var p=target.population;
		return Javelin.round(RewardCalculator.getgold(p-1,p+1));
	}

	@Override
	public boolean validate(Diplomacy d){
		return getgold()>0&&getsquad()!=null;
	}

	@Override
	public String getname(){
		return "Request gold from "+target;
	}

	@Override
	public void act(Diplomacy d){
		var gold=0;
		while(gold==0)
			gold=getgold();
		getsquad().gold+=gold;
		String result=target+" gives $"+Javelin.format(gold)
				+" to a squad in their district!";
		Javelin.message(result,true);
	}
}
