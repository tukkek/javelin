package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.enchantment.compulsion.Heroism;

/**
 * @see javelin.model.unit.abilities.spell.enchantment.compulsion.Heroism
 * @see Heroism
 * @author alex
 */
public class Heroic extends Condition{
	public Heroic(Spell s,Integer longtermp){
		super("heroic",s,Float.MAX_VALUE,longtermp,Effect.POSITIVE);
	}

	public Heroic(Spell s){
		this(s,1);
	}

	@Override
	public void start(final Combatant c){
		final Monster m=c.source;
		c.source=m.clone();
		raiseallattacks(m,+2,+0);
		raisesaves(m,+2);
	}

	@Override
	public void end(final Combatant c){
		final Monster m=c.source;
		c.source=m.clone();
		raiseallattacks(m,-2,-0);
		m.fort-=2;
		m.ref-=2;
		m.addwill(-2);
	}
}
