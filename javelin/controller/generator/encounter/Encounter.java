package javelin.controller.generator.encounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Combatant;

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
		final ArrayList<Combatant> encounter =
				new ArrayList<Combatant>(group.size());
		for (final Combatant m : group) {
			encounter.add(new Combatant(null, m.source.clone(), true));
		}
		return encounter;
	}

	public int rate() {
		return ChallengeRatingCalculator.calculateel(group);
	}

	@Override
	public String toString() {
		final HashMap<String, Integer> count = new HashMap<String, Integer>();
		for (final Combatant m : group) {
			final Integer n = count.get(m.source.toString());
			count.put(m.source.toString(), n == null ? 1 : n + 1);
		}
		return count.toString();
	}

	public int size() {
		return group.size();
	}
}