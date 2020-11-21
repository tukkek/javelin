package javelin.controller.fight;

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
		friendly=Combatant.STATUSWOUNDED;
		hide=false;
		bribe=false;
		canflee=false;
	}

	@Override
	public Integer getel(int teamel){
		return teamel;
	}
}