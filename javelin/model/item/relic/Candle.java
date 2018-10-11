package javelin.model.item.relic;

import javelin.Javelin;
import javelin.controller.action.target.Target;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/** Brings a single creature to 1hp. */
public class Candle extends Relic{
	/** Constructor. */
	public Candle(int level){
		super("Candle of searing",level);
		usedinbattle=true;
		usedoutofbattle=false;
	}

	@Override
	protected boolean activate(Combatant user){
		new Target(""){
			@Override
			protected int calculatehitdc(Combatant active,Combatant target,
					BattleState s){
				return 1;
			}

			@Override
			protected void attack(Combatant active,Combatant target,BattleState s){
				target.hp=1;
				Javelin.message("A roaring column of flame engulfs "+target+"!",
						Javelin.Delay.BLOCK);
			}
		}.perform(user);
		return true;
	}

}
