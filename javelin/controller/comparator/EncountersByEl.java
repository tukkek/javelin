package javelin.controller.comparator;

import java.util.Comparator;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.model.unit.Combatants;

public class EncountersByEl implements Comparator<Combatants> {
	public static final EncountersByEl INSTANCE = new EncountersByEl();

	private EncountersByEl() {
		// singleton
	}

	@Override
	public int compare(Combatants a, Combatants b) {
		return ChallengeCalculator.calculateel(a)
				- ChallengeCalculator.calculateel(b);
	}
}
