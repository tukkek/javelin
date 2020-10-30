package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.enchantment.compulsion.HoldMonster;

/**
 * @see HoldMonster
 * @author alex
 */
public class Paralyzed extends Condition{
	private int dex;
	private int delta;

	public Paralyzed(float expireatp,Spell s){
		super("paralyzed",s,expireatp,Effect.NEGATIVE);
	}

	@Override
	public void start(Combatant c){
		c.source=c.source.clone();
		dex=c.source.dexterity;
		delta=(int)Math.round(Math.floor(dex/2f));
		c.source.changedexteritymodifier(-delta);
		c.ap=expireat;
	}

	@Override
	public void end(Combatant c){
		c.source=c.source.clone();
		c.source.changedexteritymodifier(+delta);
	}
}
