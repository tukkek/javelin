package javelin.controller.content.upgrade.ability;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseWisdom extends RaiseAbility{
	public static final RaiseAbility SINGLETON=new RaiseWisdom();

	private RaiseWisdom(){
		super("wisdom");
	}

	@Override
	int getabilityvalue(Monster m){
		return m.wisdom;
	}

	@Override
	boolean setattribute(Combatant m,int l){
		m.source.changewisdomscore(+2);
		return true;
	}

	@Override
	public int getattribute(Monster source){
		return source.wisdom;
	}
}
