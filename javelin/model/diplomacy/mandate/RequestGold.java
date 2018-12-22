package javelin.model.diplomacy.mandate;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.diplomacy.Diplomacy;
import javelin.model.diplomacy.Relationship;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.District;

/**
 * Adds gold to a {@link Squad} standing in the respective {@link District}.
 *
 * @author alex
 */
public class RequestGold extends Mandate{
	/** Reflection constructor. */
	public RequestGold(Relationship r){
		super(r);
	}

	int getgold(){
		var population=target.town.population;
		int gold=RewardCalculator.getgold(population-1,population+1);
		if(target.getstatus()==Relationship.INDIFFERENT) gold/=2;
		return gold;
	}

	@Override
	public boolean validate(Diplomacy d){
		return target.getstatus()>=Relationship.INDIFFERENT&&getgold()!=0
				&&getsquad()!=null;
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
		String result=target+" gives $"+gold+" to a squad in their district!";
		Javelin.message(result,true);
	}
}
