package javelin.controller.content.upgrade.ability;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseStrength extends RaiseAbility{
	public static final RaiseAbility SINGLETON=new RaiseStrength();

	private RaiseStrength(){
		super("strength");
	}

	@Override
	int getabilityvalue(Monster m){
		return m.strength;
	}

	@Override
	boolean setattribute(Combatant m,int i){
		m.source.changestrengthmodifier(+1);
		return true;
	}

	@Override
	public int getattribute(Monster source){
		return source.strength;
	}
}
