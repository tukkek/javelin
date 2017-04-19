package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Siege;
import javelin.model.item.Key;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Chest;
import javelin.model.world.location.town.labor.expansive.Settler;
import tyrant.mikera.engine.RPG;

/**
 * Represents all the resource types found in the game: gold, experience, keys,
 * labor and rubies. Usually only 2 are offered per instance though, to increase
 * randomization.
 * 
 * Since the actual fight gives no xp or gold these results are doubled as
 * treasure.
 * 
 * @author alex
 */
public class Trove extends Fortification {
	enum Reward {
		GOLD, EXPERIENCE, KEY, WORKER, RUBY;

		static Reward getrandom() {
			Reward[] all = values();
			Reward r = all[RPG.r(0, all.length - 1)];
			return World.SCENARIO && r == KEY ? getrandom() : r;
		}
	}

	static final String DESCRIPTION = "A treasure trove";
	Key key = null;
	Reward[] rewards = new Reward[2];
	List<Combatant> originalgarrison = new ArrayList<Combatant>();

	/** Constructor. */
	public Trove() {
		super(DESCRIPTION, DESCRIPTION, 1, 20);
		rewards[0] = Reward.getrandom();
		while (rewards[1] == null || rewards[0] == rewards[1]) {
			rewards[1] = Reward.getrandom();
		}
		descriptionknown += " (" + describe(rewards[0]) + " or "
				+ describe(rewards[1]) + ")";
		discard = false;
		vision = 0;
		link = false;
	}

	String describe(Reward reward) {
		if (reward == Reward.KEY) {
			if (key == null) {
				key = Key.generate();
			}
			return key.toString().toLowerCase();
		}
		return reward.toString().toLowerCase();
	}

	@Override
	protected Siege fight() {
		Siege s = super.fight();
		s.rewardgold = false;
		s.rewardxp = false;
		return s;
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		super.generategarrison(minlevel, maxlevel);
		for (Combatant c : garrison) {
			originalgarrison.add(c.clone());
		}
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		int input = parse(Javelin
				.prompt("What will you take as a spoil from the battle? (press any other key to leave)\n\n"
						+ "1 - " + describe(rewards[0]) + "\n" + "2 - "
						+ describe(rewards[1])))
				- 1;
		if (input == 0 || input == 1) {
			String reward = reward(rewards[input]);
			if (reward != null) {
				Javelin.message(reward, false);
			}
			remove();
		}
		return true;
	}

	String reward(Reward reward) {
		if (reward == Reward.EXPERIENCE) {
			return RewardCalculator.rewardxp(Squad.active.members,
					Squad.active.members, originalgarrison, 2);
		}
		if (reward == Reward.GOLD) {
			int gold = RewardCalculator.receivegold(originalgarrison) * 2;
			Squad.active.gold += gold;
			return "Party receives $" + gold;
		}
		if (reward == Reward.WORKER) {
			Settler.produce(null, Squad.active);
			return "A worker joins the party! It can be assigned to any friendly town.";
		}
		if (reward == Reward.KEY) {
			key.grab();
			return null;
		}
		if (reward == Reward.RUBY) {
			return Chest.takeruby(1);
		}
		throw new RuntimeException(reward + " #unknownreward");
	}

	int parse(Character input) {
		try {
			return Integer.parseInt(Character.toString(input));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}
}