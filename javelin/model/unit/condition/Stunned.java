package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

public class Stunned extends Condition{

	public Stunned(Combatant c,Integer casterlevelp){
		super(c,"stunned",Effect.NEGATIVE,casterlevelp,c.ap+1);
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
