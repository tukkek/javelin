package javelin.model.world.location.unique.minigame;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.Rush;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.view.screen.WorldScreen;
import javelin.view.screen.town.RecruitScreen;
import tyrant.mikera.engine.RPG;

/**
 * Mini-game that allows player to recruit units against a swarm of incoming
 * enemies.
 * 
 * @author alex
 */
public class DungeonRush extends UniqueLocation {
	/** If <code>true</code> will inhibit monster from being spawned. */
	public static final boolean DEBUG = false;

	static final int MINIMUMSPAWNERS = 4;
	static final String DESCRITPION = "Dungeon rush";

	/**
	 * Units that can be created in battle. Winning battles allows you to spawn
	 * better units, losing sets you back.
	 */
	public ArrayList<Monster> spawners =
			new ArrayList<Monster>(MINIMUMSPAWNERS);

	/**
	 * Since it would be awkward to show things like CR1.25 to the player better
	 * multiply it by some factor.
	 */
	public static final int PLAYERMANAMULTIPLIER = 10;

	/** Constructor. */
	public DungeonRush() {
		super(DESCRITPION, DESCRITPION, 0, 0);
		while (spawners.size() < MINIMUMSPAWNERS) {
			upgrade(false);
		}
	}

	/**
	 * Adds a new option to {@link #spawners}.
	 * 
	 * @param humaninteraction
	 *            If <code>true</code> let's the player decide which unit to
	 *            upgrade to.
	 */
	public void upgrade(boolean humaninteraction) {
		ArrayList<Float> crs =
				new ArrayList<Float>(Javelin.MONSTERSBYCR.keySet());
		int i = 0;
		while (crs.get(i) < 1) {
			i += 1;
			if (i >= crs.size()) {
				return;
			}
		}
		i += spawners.size();
		List<Monster> tier = Javelin.MONSTERSBYCR.get(crs.get(i));
		Monster spawner;
		if (humaninteraction) {
			spawner = tier.get(Javelin.choose(
					"Victory! Which new spawner do you want to acquire?", tier,
					true, true));
		} else {
			spawner = RPG.pick(tier);
		}
		spawners.add(spawner);
		StateManager.save(true, StateManager.SAVEFILE);
	}

	/**
	 * Removes most powerful of the {@link #spawners}.
	 * 
	 * @return
	 */
	public Monster downgrade() {
		if (spawners.size() <= MINIMUMSPAWNERS) {
			return null;
		}
		int last = spawners.size() - 1;
		Monster removed = spawners.get(last);
		spawners.remove(last);
		StateManager.save(true, StateManager.SAVEFILE);
		return removed;
	}

	@Override
	public List<Combatant> getcombatants() {
		return null;
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		// empty
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		ArrayList<String> choices = new ArrayList<String>(spawners.size());
		for (Monster m : spawners) {
			choices.add(m + " (" + Math.round(m.challengeRating * 100) + "XP)");
		}
		String prompt =
				"Which creature do you wish to spawn?\n\nYou currently have "
						+ RecruitScreen.sumxp() + " XP";
		int choice = Javelin.choose(prompt, choices, true, false);
		Javelin.app.switchScreen(WorldScreen.active);
		if (choice == -1) {
			return true;
		}
		Monster recruit = spawners.get(choice);
		if (RecruitScreen.canbuy(recruit.challengeRating * 100)) {
			RecruitScreen.spend(recruit.challengeRating);
			Squad.active.members.add(new Combatant(recruit.clone(), true));
		} else {
			Javelin.message(
					"You don't have enough experience to acquire this unit...",
					true);
		}
		return true;
	}

	/** Starts a {@link Rush}. */
	static public void start() {
		if (Javelin.prompt(
				"Start a dungeon rush match?\n\nPress ENTER start or any other key to cancel...") == '\n') {
			throw new StartBattle(new Rush(get()));
		}
	}

	/**
	 * @return The Dungoen Rush location in the {@link World}.
	 */
	public static DungeonRush get() {
		return (DungeonRush) WorldActor.getall(DungeonRush.class).get(0);
	}

	@Override
	protected void generate() {
		while (x < 0 || !Terrain.get(x, y).equals(Terrain.MOUNTAINS)) {
			super.generate();
		}
	}
}
