package javelin.model.unit.condition;

import java.util.ArrayList;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.necromancy.Doom;
import javelin.model.unit.attack.Attack;
import javelin.model.unit.attack.AttackSequence;

/**
 * @see Doom
 * @author alex
 */
public class Shaken extends Condition{
	/** Constructor. */
	public Shaken(float expireatp,Spell s){
		super("shaken",s,expireatp,1,Effect.NEGATIVE);
	}

	@Override
	public void start(Combatant c){
		c.source=c.source.clone();
		penalizeattacks(-2,c.source.melee);
		penalizeattacks(-2,c.source.ranged);
		c.source.fort-=2;
		c.source.ref-=2;
		c.source.addwill(-2);
	}

	void penalizeattacks(int bonus,ArrayList<AttackSequence> sequences){
		for(AttackSequence sequence:sequences)
			for(Attack a:sequence)
				a.bonus+=bonus;
	}

	@Override
	public void end(Combatant c){
		c.source=c.source.clone();
		penalizeattacks(+2,c.source.melee);
		penalizeattacks(+2,c.source.ranged);
		c.source.fort+=2;
		c.source.ref+=2;
		c.source.addwill(+2);
	}
}
