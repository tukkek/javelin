package javelin.controller.content.fight.mutator.mode;

import javelin.Javelin;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.fight.mutator.Mutator;
import javelin.controller.exception.GaveUp;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatants;

/**
 * A "director"/"dynamic" type of {@link Fight}. Conceptually only one at a time
 * should be used per battle.
 *
 * @author alex
 */
public abstract class FightMode extends Mutator{
	/** @return Enemies to start {@link BattleState#redteam} with. */
	public abstract Combatants generate(Fight f) throws GaveUp;

	@Override
	public void prepare(Fight f){
		super.prepare(f);
		try{
			Fight.state.redteam=generate(f);
		}catch(GaveUp e){
			if(Javelin.DEBUG) throw new RuntimeException(e);
		}
	}
}