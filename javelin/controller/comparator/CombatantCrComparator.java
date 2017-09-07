package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.attack.Combatant;

public class CombatantCrComparator implements Comparator<Combatant> {
	@Override
	public int compare(Combatant o1, Combatant o2) {
		return Float.compare(o1.source.challengerating,
				o2.source.challengerating);
	}
}
