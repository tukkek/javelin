package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Grants a temporary {@link Spell} to a {@link Combatant}. It's removed from
 * its spell list
 *
 * @author alex
 *
 */
public class TemporarySpell extends Condition{
	Spell spell;

	public TemporarySpell(String name,Spell s,Combatant c){
		super(c,name,Effect.NEUTRAL,null,Float.MAX_VALUE,24);
		spell=s;
		stack=true;
	}

	@Override
	public void start(Combatant c){
		var spell=c.spells.get(this.spell.getClass());
		if(spell==null){
			spell=this.spell.clone();
			c.spells.add(spell);
		}else
			spell.perday+=1;
	}

	@Override
	public void end(Combatant c){
		var spell=c.spells.get(this.spell.getClass());
		remove(c,spell);
	}

	void remove(Combatant c,Spell s){
		if(s==null) return;
		if(s.perday==1)
			c.spells.remove(s);
		else
			s.perday-=1;
	}

	@Override
	public void transfer(Combatant from,Combatant to){
		super.transfer(from,to);
		var spell=from.spells.get(this.spell.getClass());
		if(spell!=null&&spell.exhausted()){
			remove(to,spell);
			to.removecondition(this);
		}
	}
}