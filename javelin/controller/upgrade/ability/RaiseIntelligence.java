package javelin.controller.upgrade.ability;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * @see RaiseAbility
 */
public class RaiseIntelligence extends RaiseAbility {
	/** Singleton instance. */
	public static final RaiseAbility INSTANCE = new RaiseIntelligence();

	RaiseIntelligence() {
		super("intelligence");
	}

	@Override
	int getabilityvalue(Monster m) {
		return m.intelligence;
	}

	@Override
	boolean setattribute(Combatant m, int l) {
		m.source.raiseintelligence(+2);
		return true;
	}

	@Override
	public boolean apply(Combatant m) {
		return m.source.intelligence != 0 && super.apply(m);
	}
}
