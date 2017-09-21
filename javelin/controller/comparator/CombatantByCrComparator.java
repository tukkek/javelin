package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.attack.Combatant;

public class CombatantByCrComparator implements Comparator<Combatant> {
	public static final CombatantByCrComparator SINGLETON = new CombatantByCrComparator();

	private CombatantByCrComparator() {
		// prevent instantiation
	}

	@Override
	public int compare(Combatant o1, Combatant o2) {
		return Float.compare(o1.source.challengerating,
				o2.source.challengerating);
	}
}
