package javelin.controller.comparator;

import java.util.Comparator;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.attack.Combatant;

/**
 * Helps sort a list of {@link Combatant} by level.
 * 
 * @author alex
 */
public class DescendingLevelComparator implements Comparator<Combatant> {
	public static final DescendingLevelComparator SINGLETON = new DescendingLevelComparator();

	private DescendingLevelComparator() {
		super();
	}

	@Override
	public int compare(Combatant arg0, Combatant arg1) {
		return new Integer(sumcrandxp(arg0)).compareTo(sumcrandxp(arg1));
	}

	int sumcrandxp(Combatant arg0) {
		return -Math.round(100 * (arg0.xp.floatValue()
				+ ChallengeRatingCalculator.calculatecr(arg0.source)));
	}
}