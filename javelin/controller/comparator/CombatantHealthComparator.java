package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.attack.Combatant;

public class CombatantHealthComparator implements Comparator<Combatant> {
	public static final CombatantHealthComparator SINGLETON = new CombatantHealthComparator();

	private CombatantHealthComparator() {
		// use SINGLETON
	}

	@Override
	public int compare(Combatant o1, Combatant o2) {
		return o1.getnumericstatus() - o2.getnumericstatus();
	}
}
