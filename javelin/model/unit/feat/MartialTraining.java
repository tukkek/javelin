package javelin.model.unit.feat;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.challenge.factor.FeatsFactor;
import javelin.model.unit.abilities.discipline.Discipline;
import javelin.model.unit.abilities.discipline.Maneuver;
import javelin.model.unit.abilities.discipline.Maneuvers;
import javelin.model.unit.attack.Combatant;
import tyrant.mikera.engine.RPG;

/**
 * TODO how to allow player to select the maneuvers to learn?
 * 
 * @author alex
 */
public class MartialTraining extends Feat {
	/**
	 * At first will only implement up to lavel 4 TODO
	 */
	private static final int MAXLEVEL = 4;

	/**
	 * Double that of your typical feat because it implies an Extra Readied
	 * Maneuver feat so that both maneuvers you gain while leveling up are
	 * readied when combat starts.
	 */
	static final float CR = FeatsFactor.CR * 2;

	private static final String[] RANK = new String[] { "Novice", "Student",
			"Teacher", "Master", "Grandmaster", "Legend", };

	Discipline discipline;
	int level = 0;

	public MartialTraining(Discipline d) {
		super(d.name + " training");
		this.discipline = d;
		cr = CR;
	}

	@Override
	public boolean apply(Combatant c) {
		if (level == MAXLEVEL) {
			return false;
		}
		if (!validate(c)) {
			return false;
		}
		if (c.source.hasfeat(this) || super.apply(c)) {
			upgrade(c);
			return true;
		}
		return false;
	}

	boolean validate(Combatant c) {
		int nextlevel = level + 1;
		int minimum = 1 + nextlevel * 2;
		return c.source.getbaseattackbonus() >= minimum
				|| c.source.skills.knowledge >= minimum;
	}

	void upgrade(Combatant c) {
		level += 1;
		ArrayList<Maneuver> trainable = discipline.getmaneuvers(level);
		Maneuvers known = c.disciplines.get(discipline);
		trainable.removeAll(known);
		int trained = 0;
		int level = trainable.get(0).level;
		while (trained < 2) {
			if (!learn(trainable, level, c)) {
				level -= 1;
			}
		}
	}

	boolean learn(ArrayList<Maneuver> trainable, int level, Combatant c) {
		Maneuvers tier = new Maneuvers();
		for (Maneuver m : trainable) {
			if (m.level == level && m.validate(c)) {
				tier.add(m);
			}
		}
		if (tier.isEmpty()) {
			return false;
		}
		Maneuver m = RPG.pick(tier);
		boolean failed = c.addmaneuver(discipline, m);
		if (Javelin.DEBUG && failed) {
			final String error = "Invalid maneuver " + m + " for " + c;
			throw new RuntimeException(error);
		}
		return true;
	}

	@Override
	public String toString() {
		return name + " (" + RANK[level - 1] + ")";
	}
}
