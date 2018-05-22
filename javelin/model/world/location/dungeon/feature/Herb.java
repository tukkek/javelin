package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.challenge.RewardCalculator;
import javelin.model.item.Potion;
import javelin.model.unit.skill.Survival;
import tyrant.mikera.engine.RPG;

/**
 * Similar to a {@link Chest} but requires someone with enough {@link Survival}
 * and results in a higher-than average {@link Potion} treasure.
 *
 * @see RewardCalculator
 * @author alex
 */
public class Herb extends Feature {
	public static final int MAXLEVEL;

	static final List<Potion> POTIONS = Potion.getpotions();
	static final int MAXCOPIES = 5;

	static {
		int ceiling = POTIONS.get(POTIONS.size() - 1).price;
		ceiling *= MAXCOPIES;
		int level = 1;
		while (RewardCalculator.getgold(level + 1) <= ceiling) {
			level += 1;
		}
		MAXLEVEL = level;
	}

	/** Constructor. */
	public Herb(int xp, int yp) {
		super(xp, yp, "dungeonherb");
	}

	@Override
	public boolean activate() {
		// check survival
		// 50% chance of encounter
		// generate + grab
		return false;
	}

	public static List<Potion> generate(int level) {
		Collections.shuffle(POTIONS);
		ArrayList<Potion> potions = new ArrayList<>(MAXCOPIES);
		int reward = RewardCalculator.getgold(level);
		while (potions.isEmpty()) {
			int pool = reward;
			Potion p = RPG.pick(POTIONS);
			for (int i = 0; i < MAXCOPIES && pool > 0; i++) {
				potions.add((Potion) p.clone());
				pool -= p.price;
			}
			if (pool > 0 || pool < -reward * 10) {
				potions.clear();
			}
		}
		return potions;
	}

	public static String describe(List<Potion> potions) {
		int amount = potions.size();
		String description = potions.get(0).toString().toLowerCase();
		return (amount == 1 ? "a" : amount) + " " + description;
	}
}
