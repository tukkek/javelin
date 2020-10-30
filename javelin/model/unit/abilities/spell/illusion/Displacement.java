package javelin.model.unit.abilities.spell.illusion;

import javelin.controller.ai.ChanceNode;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Touch;
import javelin.model.unit.condition.Condition;

/**
 * See SRD;
 *
 * @author alex
 */
public class Displacement extends Touch{
	class Blinking extends Condition{
		Blinking(float expireatp,Spell s){
			super("blinking",s,expireatp,Effect.POSITIVE);
		}

		@Override
		public void start(Combatant c){
			c.source=c.source.clone();
			c.source.misschance+=.5;
		}

		@Override
		public void end(Combatant c){
			c.source=c.source.clone();
			c.source.misschance-=.5;
		}
	}

	protected int turns=6;

	/** Constructor. */
	public Displacement(){
		this("Displacement",3,ChallengeCalculator.ratespell(3));
		isrune=new Blinking(Float.MAX_VALUE,this);
	}

	/** Constructor. */
	protected Displacement(String name,int levelp,float incrementcost){
		super(name,levelp,incrementcost);
		castinbattle=true;
		castonallies=true;
		/*potion is Blink*/
	}

	@Override
	public String cast(Combatant caster,Combatant target,boolean saved,
			BattleState s,ChanceNode cn){
		target.addcondition(new Blinking(caster.ap+turns,this));
		return target+" is blinking!";
	}
}
