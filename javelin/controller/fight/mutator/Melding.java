package javelin.controller.fight.mutator;

import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;

/** @see Meld */
public class Melding extends Mutator{
	@Override
	public void die(Combatant c,BattleState s,Fight fight){
		super.die(c,s,fight);
		if(!c.summoned&&c.getnumericstatus()==Combatant.STATUSDEAD
				&&c.source.isalive()){
			var m=new Meld(c.location[0],c.location[1],s.next.ap+1,c);
			s.meld.add(m);
		}
	}
}
