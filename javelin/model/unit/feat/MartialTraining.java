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
	/**
	 * Number of {@link Maneuver}s that can be learned after upgrading this.
	 * 
	 * @see Combatant#postupgrade(boolean, javelin.controller.upgrade.Upgrade)
	 * @see Combatant#postupgradeautomatic(boolean,
	 *      javelin.controller.upgrade.Upgrade)
	 */
	int slots = 2;

	public MartialTraining(Discipline d) {
		super(d.name + " training");
		this.discipline = d;
		cr = CR;
	}

	@Override
	public boolean apply(Combatant c) {
		c.source = c.source.clone();
		if (level == MAXLEVEL) {
			return false;
		}
		if (!validate(c)) {
			return false;
		}
		int i = c.source.feats.indexOf(this);
		if (i == -1) {
			return super.apply(c);
		}
		slots += 2;
		return true;
	}

	boolean validate(Combatant c) {
		int nextlevel = level + 1;
		int minimum = 1 + nextlevel * 2;
		return c.source.getbaseattackbonus() >= minimum
				|| c.source.skills.knowledge >= minimum;
	}

	@Override
	public void postupgradeautomatic(Combatant c) {
		while (slots > 0) {
			level += 1;
			int picked = 0;
			ArrayList<Maneuver> trainable = gettrainable(c, 2 - picked);
			int level = trainable.get(0).level;
			while (picked < 2) {
				if (pick(trainable, level, c)) {
					picked -= 1;
				} else {
					level -= 1;
				}
			}
		}
	}

	ArrayList<Maneuver> gettrainable(Combatant c, int picks) {
		Maneuvers trainable = discipline.getmaneuvers(level);
		Maneuvers known = c.disciplines.get(discipline);
		if (known != null) {
			trainable.removeAll(known);
		}
		if (trainable.size() < picks) {
			final String error = "Not enough trainable maneuvers for discipline "
					+ discipline + " at level " + level + "!";
			throw new RuntimeException(error);
		}
		return trainable;
	}

	boolean pick(ArrayList<Maneuver> trainable, int level, Combatant c) {
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
		slots -= 1;
		return true;
	}

	@Override
	public String toString() {
		final String rank = level == 0 ? ""
				: " (" + getrank().toLowerCase() + ")";
		return name + rank;
	}

	String getrank() {
		return RANK[level - 1];
	}

	@Override
	public void postupgrade(Combatant c) {
		while (slots > 0) {
			level += 1;
			int pick = 2;
			while (pick > 0) {
				ArrayList<Maneuver> trainable = gettrainable(c, pick);
				String prompt = "You have advanced to being a "
						+ getrank().toLowerCase() + " in the path of the "
						+ discipline + "!\n" + "You can now select " + pick
						+ " extra maneuver(s). What will you learn?";
				int choice = Javelin.choose(prompt, trainable, true, true);
				c.addmaneuver(discipline, trainable.get(choice));
				pick -= 1;
				slots -= 1;
			}
		}
	}

	@Override
	public Feat generate(String name2) {
		return new MartialTraining(discipline);
	}
}
