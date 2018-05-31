package javelin.model.world.location.dungeon.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Debug;
import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomDungeonEncounter;
import javelin.controller.table.dungeon.FeatureModifierTable;
import javelin.model.item.Item;
import javelin.model.item.Potion;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.unit.skill.Survival;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;

/**
 * Similar to a {@link Chest} but requires someone with enough {@link Survival}
 * and results in a higher-than average {@link Potion} treasure.
 *
 * @see RewardCalculator
 * @author alex
 */
public class Herb extends Feature {
	/**
	 * Unfortunately {@link Potion}s are on the lower end of magic
	 * {@link Item}s, so it's hard to scale this {@link Dungeon} feature
	 * indefinitely. Instead, set this max allowed level as per
	 * {@link Dungeon#level}.
	 *
	 * Note that this feature will generate one or multiple instances of the
	 * same potion type.
	 */
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

	int dc = 10 + Dungeon.active.level
			+ Dungeon.gettable(FeatureModifierTable.class).rollmodifier();
	List<Potion> loot = generate(Dungeon.active.level);

	/** Constructor. */
	public Herb() {
		super("dungeonherb");
		remove = false;
	}

	@Override
	public boolean activate() {
		String description = describe(loot);
		Squad s = Squad.active;
		Combatant survivalist = s.getbest(Skill.SURVIVAL);
		if (survivalist.taketen(Skill.SURVIVAL) < dc) {
			String text = survivalist + " is not familiar with this plant...";
			if (s.getbest(Skill.KNOWLEDGE).taketen(Skill.KNOWLEDGE) >= dc) {
				text = "A more skilled group could turn these herbs into "
						+ description + "...";
			}
			Javelin.message(text, false);
			return true;
		}
		if (RPG.chancein(2) && !Debug.disablecombat) {
			String interupted = "You are interrupted while extracting the herbs!";
			Javelin.message(interupted, false);
			throw new StartBattle(new RandomDungeonEncounter(Dungeon.active));
		}
		s.hourselapsed += 1;
		String success = "You extract " + description + " from the herbs!";
		Javelin.message(success, false);
		for (Potion p : loot) {
			p.grab();
		}
		remove();
		return false;
	}

	/**
	 * @param level
	 *            Approximate reward level.
	 * @return Generates a set of potions.
	 * @see RewardCalculator
	 * @see #MAXLEVEL
	 */
	static List<Potion> generate(int level) {
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

	/**
	 * @param potions
	 *            A number of the same type of item.
	 * @return "a potion of cure light wounds", "4x potion of barkskin"...
	 */
	static String describe(List<? extends Item> potions) {
		int amount = potions.size();
		String description = potions.get(0).toString().toLowerCase();
		return (amount == 1 ? "a" : amount + "x") + " " + description;
	}
}