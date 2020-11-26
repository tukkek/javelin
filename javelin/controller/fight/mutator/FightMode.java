package javelin.controller.fight.mutator;

import javelin.Javelin;
import javelin.controller.exception.GaveUp;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatants;

/**
 * A "director"/"dynamic" type of {@link Fight}. Conceptually only one at a time
 * should be used per battle.
 *
 * @author alex
 */
public abstract class FightMode extends Mutator{
	/** @return Enemies to start {@link BattleState#redTeam} with. */
	public abstract Combatants generate(Fight f) throws GaveUp;

	@Override
	public void prepare(Fight f){
		super.prepare(f);
		try{
			Fight.state.redTeam=generate(f);
		}catch(GaveUp e){
			if(Javelin.DEBUG) throw new RuntimeException(e);
		}
	}
}