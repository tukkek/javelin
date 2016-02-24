package javelin.controller.encounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * A group of monsters to be fought against.
 * 
 * @author alex
 */
public class Encounter {
	public final List<Combatant> group;

	public Encounter(List<Combatant> groupp) {
		group = groupp;
	}

	public ArrayList<Combatant> generate() {
		final ArrayList<Combatant> encounter = new ArrayList<Combatant>();
		for (final Combatant m : group) {
			encounter.add(m);
		}
		return encounter;
	}

	public int rate() {
		return ChallengeRatingCalculator.calculateElSafe(group);
	}

	@Override
	public String toString() {
		final HashMap<Monster, Integer> count = new HashMap<Monster, Integer>();
		for (final Combatant m : group) {
			final Integer n = count.get(m.source);
			count.put(m.source, n == null ? 1 : n + 1);
		}
		return count.toString();
	}

	public int size() {
		return group.size();
	}
}