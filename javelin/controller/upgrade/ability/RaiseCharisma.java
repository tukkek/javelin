package javelin.controller.upgrade.ability;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseCharisma extends RaiseAbility {
	/** Singleton instance. */
	public static final RaiseAbility SINGLETON = new RaiseCharisma();

	RaiseCharisma() {
		super("charisma");
	}

	@Override
	int getabilityvalue(Monster m) {
		return m.charisma;
	}

	@Override
	boolean setattribute(Combatant m, int l) {
		m.source.raisecharisma(+1);
		return true;
	}

	@Override
	public int getattribute(Monster source) {
		return source.charisma;
	}

}
