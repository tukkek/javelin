package javelin.controller.comparator;

import java.util.Comparator;

import javelin.controller.challenge.CrCalculator;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;

/**
 * Compares combatants by their {@link Monster#challengerating}. This doesn't
 * update {@link Monster#challengerating} as the comparison goes on since the
 * comparison isn't a linear operation - if you need to, use
 * {@link CrCalculator#updatecr(java.util.ArrayList)} before sorting with this.
 * 
 * @see CrCalculator#calculatecr(Monster)
 * @author alex
 */
public class CombatantByCr implements Comparator<Combatant> {
	/** Trusts {@link Monster#challengerating}. */
	public static final CombatantByCr SINGLETON = new CombatantByCr(false);

	private CombatantByCr(boolean update) {
		// prevents instantiation
	}

	@Override
	public int compare(Combatant o1, Combatant o2) {
		final Float cr1 = o1.source.challengerating;
		final Float cr2 = o2.source.challengerating;
		return Float.compare(cr1, cr2);
	}
}
