package javelin.controller.encounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.reader.MonsterReader;
import javelin.model.unit.Monster;

public class Encounter {
	private final List<Monster> group;
	public boolean aquatic = false;

	public Encounter(List<Monster> groupp) {
		group = groupp;
		for (Monster m : group) {
			if (MonsterReader.AQUATICMONSTERS.contains(m)) {
				aquatic = true;
				break;
			}
		}
	}

	public ArrayList<Monster> generate() {
		final ArrayList<Monster> encounter = new ArrayList<Monster>();
		for (final Monster m : group) {
			encounter.add(m);
		}
		return encounter;
	}

	public int rate() {
		return ChallengeRatingCalculator.calculateSafe(group);
	}

	@Override
	public String toString() {
		final HashMap<Monster, Integer> count = new HashMap<Monster, Integer>();
		for (final Monster m : group) {
			final Integer n = count.get(m);
			count.put(m, n == null ? 1 : n + 1);
		}
		return count.toString();
	}

	public int size() {
		return group.size();
	}
}