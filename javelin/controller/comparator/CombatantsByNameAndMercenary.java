package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.attack.Combatant;

public class CombatantsByNameAndMercenary implements Comparator<Combatant> {
	public static final CombatantsByNameAndMercenary SINGLETON = new CombatantsByNameAndMercenary();

	private CombatantsByNameAndMercenary() {
		// use singleton
	}

	@Override
	public int compare(Combatant o1, Combatant o2) {
		boolean mercenary1 = o1.mercenary;
		boolean mercenary2 = o2.mercenary;
		if (mercenary1 && !mercenary2) {
			return +1;
		} else if (!mercenary1 && mercenary2) {
			return -1;
		}
		return o1.toString().compareTo(o2.toString());
	}
}
