package javelin.controller.upgrade.ability;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseIntelligence extends RaiseAbility{
	/** Singleton instance. */
	public static final RaiseAbility SINGLETON=new RaiseIntelligence();

	RaiseIntelligence(){
		super("intelligence");
	}

	@Override
	int getabilityvalue(Monster m){
		return m.intelligence;
	}

	@Override
	boolean setattribute(Combatant m,int l){
		m.source.changeintelligencescore(+2);
		return true;
	}

	@Override
	public boolean apply(Combatant c){
		return c.source.intelligence!=0&&super.apply(c);
	}

	@Override
	public int getattribute(Monster source){
		return source.intelligence;
	}
}
