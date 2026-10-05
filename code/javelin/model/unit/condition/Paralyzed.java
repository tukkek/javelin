package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.enchantment.compulsion.HoldMonster;

/**
 * @see HoldMonster
 * @author alex
 */
public class Paralyzed extends Condition{
	int delta;

	public Paralyzed(float expireatp,Spell s){
		super("paralyzed",s,expireatp,Effect.NEGATIVE);
	}

	@Override
	public void start(Combatant c){
		var d=c.source.dexterity;
		delta=(int)Math.round(Math.floor(d/2f));
		c.source.changedexteritymodifier(-delta);
		c.ap=expireat;
	}

	@Override
	public void end(Combatant c){
		c.source.changedexteritymodifier(+delta);
	}
}
