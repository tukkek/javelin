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
		super(name,null,Float.MAX_VALUE,24,Effect.NEUTRAL);
		spell=s;
		stack=true;
	}

	@Override
	public void start(Combatant c){
		var s=c.spells.get(this.spell);
		if(s==null){
			s=this.spell.clone();
			c.spells.add(s);
		}else
			s.perday+=1;
	}

	@Override
	public void end(Combatant c){
		remove(c,c.spells.get(this.spell));
	}

	void remove(Combatant c,Spell s){
		if(s==null) return;
		if(s.perday==1)
			c.spells.remove(s);
		else
			s.perday-=1;
	}

	@Override
	protected boolean expire(int time,Combatant c){
		if(super.expire(time,c)) return true;
		var spell=c.spells.get(this.spell);
		return spell!=null&&spell.exhausted();
	}
}