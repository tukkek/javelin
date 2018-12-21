package javelin.controller.fight;

import java.util.ArrayList;

import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.map.Stadium;
import javelin.model.unit.Combatant;

/**
 * Tournament event.
 *
 * @see Exhibition
 *
 * @author alex
 */
public class ExhibitionFight extends Fight{
	/** Constructor. */
	public ExhibitionFight(){
		map=new Stadium();
		meld=true;
		friendly=true;
		hide=false;
		bribe=false;
		canflee=false;
		rewardreputation=true;
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		return null;
	}

	@Override
	public Integer getel(int teamel){
		return teamel;
	}
}