package javelin.controller.content.fight.mutator;

import javelin.controller.content.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.MeldCrystal;
import javelin.model.unit.Combatant;

/** @see MeldCrystal */
public class Meld extends Mutator{
	@Override
	public void die(Combatant c,BattleState s,Fight fight){
		super.die(c,s,fight);
		if(!c.summoned&&c.source.isalive()
				&&c.getnumericstatus()==Combatant.STATUSDEAD){
			var m=new MeldCrystal(c.location[0],c.location[1],s.next.ap+1,c);
			s.meld.add(m);
		}
	}
}
