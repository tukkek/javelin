package javelin.model.unit.condition;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;

/**
 * Moves at half speed and takes a -6 penalty to Strength and Dexterity.
 *
 * @author alex
 */
public class Exhausted extends Fatigued{
	private int[] originalspeeds;

	public Exhausted(Spell s){
		super("exhausted",s,0);
	}

	@Override
	public void start(Combatant c){
		Condition fatigue=c.hascondition(Fatigued.class);
		if(fatigue!=null) c.removecondition(fatigue);
		c.source=c.source.clone();
		javelin.model.unit.Monster m=c.source;
		originalspeeds=new int[]{m.burrow,m.fly,m.swim,m.walk};
		m.burrow=m.burrow/2;
		m.fly=m.fly/2;
		m.swim=m.swim/2;
		m.walk=m.walk/2;
		m.changestrengthmodifier(-3);
		m.changedexteritymodifier(-3);
	}

	@Override
	public void end(Combatant c){
		javelin.model.unit.Monster m=c.source;
		m.burrow=Math.max(m.burrow,originalspeeds[0]);
		m.fly=Math.max(m.fly,originalspeeds[1]);
		m.swim=Math.max(m.swim,originalspeeds[2]);
		m.walk=Math.max(m.walk,originalspeeds[3]);
		m.changestrengthmodifier(+3);
		m.changedexteritymodifier(+3);
		var f=new Fatigued(spelllevel,casterlevel,Float.MAX_VALUE,0,
				Effect.NEGATIVE);
		c.addcondition(f);
	}
}
