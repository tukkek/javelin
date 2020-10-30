package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.Spell;

public class Stunned extends Condition{

	public Stunned(Combatant c,Spell s){
		super("stunned",s,c.ap+1,Effect.NEGATIVE);
	}

	@Override
	public void start(Combatant c){
		c.ap+=1;
		c.acmodifier-=2+getbonus(c);
	}

	int getbonus(Combatant c){
		return Math.max(0,Monster.getbonus(c.source.dexterity));
	}

	@Override
	public void end(Combatant c){
		c.acmodifier+=2+getbonus(c);
	}

}
