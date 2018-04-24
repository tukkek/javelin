package javelin.model.world.location.fortification;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.fight.Siege;
import javelin.controller.old.Game;
import javelin.model.item.Key;
import javelin.model.item.Ruby;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.view.screen.town.SelectScreen;
import tyrant.mikera.engine.RPG;

/**
 * Represents all the resource types found in the game: gold, experience, keys,
 * labor and rubies. Usually only 2 are offered per instance though, to increase
 * randomization.
 *
 * Since the actual fight gives no xp or gold these results are doubled as
 * treasure.
 *
 * TODO experience was a nice reward but too explotiable. You could dismiss
 * mercenaries and have a larger XP reward, or divide the squad and have only
 * reiceve all XP, which is super explotaible. If could trigger
 * {@link #reward(Reward)} just after the battle is over, this could be easily
 * solved.
 *
 * @author alex
 */
public class Trove extends Fortification {
	enum Reward {
		GOLD, EXPERIENCE, KEY, RUBY;

		static Reward getrandom() {
			Reward[] all = values();
			Reward r = all[RPG.r(0, all.length - 1)];
			return r == KEY && !World.scenario.allowkeys ? getrandom() : r;
		}
	}

	public class TroveFight extends Siege {
		public TroveFight(Location l) {
			super(l);
			rewardgold = false;
			rewardxp = false;
		}

		@Override
		public String reward() {
			String message = take();
			Game.messagepanel.clear();
			return message;
		}
	}

	static final String DESCRIPTION = "A treasure trove";
	Key key = null;
	Reward[] rewards = new Reward[2];
	List<Combatant> originalgarrison = new ArrayList<Combatant>();

	/** Constructor. */
	public Trove() {
		super(DESCRIPTION, DESCRIPTION, 1, 20);
		if (World.scenario.simpletroves) {
			rewards[0] = Reward.EXPERIENCE;
			rewards[1] = Reward.GOLD;
		} else {
			rewards[0] = Reward.getrandom();
			while (rewards[1] == null || rewards[0] == rewards[1]) {
				rewards[1] = Reward.getrandom();
			}
			if (rewards[0] == Reward.KEY || rewards[1] == Reward.KEY) {
				key = Key.generate();
			}
		}
		descriptionknown += " (" + describe(rewards[0]) + " or "
				+ describe(rewards[1]) + ")";
		discard = false;
		vision = 0;
		link = false;
	}

	String describe(Reward reward) {
		Object o = reward == Reward.KEY ? key : reward;
		return o.toString().toLowerCase();
	}

	@Override
	protected Siege fight() {
		return new TroveFight(this);
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		super.generategarrison(minlevel, maxlevel);
		for (Combatant c : garrison) {
			originalgarrison.add(c.clone());
		}
	}

	String reward(Reward reward) {
		if (reward == Reward.EXPERIENCE) {
			return RewardCalculator.rewardxp(Squad.active.members,
					originalgarrison, 2);
		}
		if (reward == Reward.GOLD) {
			int gold = RewardCalculator.receivegold(originalgarrison) * 2;
			Squad.active.gold += gold;
			return "Party receives $" + SelectScreen.formatcost(gold) + "!";
		}
		if (reward == Reward.KEY) {
			key.grab();
			return null;
		}
		if (reward == Reward.RUBY) {
			new Ruby().grab();
			return null;
		}
		throw new RuntimeException(reward + " #unknownreward");
	}

	@Override
	public List<Combatant> getcombatants() {
		return garrison;
	}

	String take() {
		remove();
		ArrayList<String> choices = new ArrayList<String>(rewards.length);
		for (Reward r : rewards) {
			choices.add(describe(r));
		}
		String prompt = "What will you take as a spoil from this trove?";
		int choice = Javelin.choose(prompt, choices, false, true);
		return reward(rewards[choice]);
	}

	@Override
	public boolean interact() {
		if (!Javelin.DEBUG) {
			return super.interact();
		}
		if (!super.interact()) {
			return false;
		}
		Javelin.message(take(), false);
		return true;
	}
}