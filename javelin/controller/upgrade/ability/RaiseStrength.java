package javelin.controller.upgrade.ability;

import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * @see RaiseAbility
 */
public class RaiseStrength extends RaiseAbility {
	public static final RaiseAbility SINGLETON = new RaiseStrength();

	private RaiseStrength() {
		super("strength");
	}

	@Override
	int getabilityvalue(Monster m) {
		return m.strength;
	}

	@Override
	boolean setattribute(Combatant m, int i) {
		m.source.changestrengthmodifier(+1);
		return true;
	}

	@Override
	public int getattribute(Monster source) {
		return source.strength;
	}
}
