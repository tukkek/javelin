package javelin.model.item.artifact;

import javelin.Javelin;
import javelin.controller.action.target.Target;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;

/** Brings a single creature to 1hp. */
public class Candle extends Artifact{
	/** Constructor. */
	public Candle(){
		super("Candle of searing");
		usedinbattle=true;
		usedoutofbattle=false;
	}

	@Override
	protected boolean activate(Combatant user){
		new Target(""){
			@Override
			protected int predictchance(Combatant c,Combatant target,BattleState s){
				return 1;
			}

			@Override
			protected void attack(Combatant c,Combatant target,BattleState s){
				target.hp=1;
				var text="A roaring column of flame engulfs "+target+"!";
				Javelin.message(text,Javelin.Delay.BLOCK);
			}
		}.perform(user);
		return true;
	}

}
