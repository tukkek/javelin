package javelin.controller;

import java.util.Comparator;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Combatant;

/**
 * Helps sort a list of {@link Combatant} by level.
 * 
 * @author alex
 */
public class DescendingLevelComparator implements Comparator<Combatant> {
	@Override
	public int compare(Combatant arg0, Combatant arg1) {
		return new Integer(sumcrandxp(arg0)).compareTo(sumcrandxp(arg1));
	}

	int sumcrandxp(Combatant arg0) {
		return -Math.round(100 * (arg0.xp.floatValue()
				+ ChallengeRatingCalculator.calculatecr(arg0.source)));
	}
}