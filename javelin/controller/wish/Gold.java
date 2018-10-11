package javelin.controller.wish;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

public class Gold extends Wish{
	public Gold(char key,WishScreen screen){
		super("gold ($"+Javelin.format(getgold(screen))+")",key,screen.rubies,false,
				screen);
	}

	static int getgold(WishScreen s){
		return Javelin.round(RewardCalculator.getgold(s.rubies+1));
	}

	@Override
	boolean wish(Combatant target){
		Squad.active.gold+=getgold(screen);
		return true;
	}
}
