package javelin.model.world.location.unique.minigame;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Rush;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.unique.UniqueLocation;
import tyrant.mikera.engine.RPG;

/**
 * Mini-game that allows player to recruit units against a swarm of incoming
 * enemies.
 * 
 * TODO allow player to recruit
 * 
 * TODO allow player to decide which {@link #upgrade()} to advance to
 * 
 * TODO allow recruiting in-loco
 * 
 * @author alex
 */
public class DungeonRush extends UniqueLocation {
	private static final int MINIMUMSPAWNERS = 4;
	private static final String DESCRITPION = "Dungeon rush";

	/**
	 * Units that can be created in battle. Winning battles allows you to spawn
	 * better units, losing sets you back.
	 */
	public ArrayList<Monster> spawners =
			new ArrayList<Monster>(MINIMUMSPAWNERS);

	/** Constructor. */
	public DungeonRush() {
		super(DESCRITPION, DESCRITPION, 0, 0);
		while (spawners.size() < MINIMUMSPAWNERS) {
			upgrade();
		}
	}

	/**
	 * Adds a new option to {@link #spawners}.
	 */
	public void upgrade() {
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
		spawners.add(RPG.pick(Javelin.MONSTERSBYCR.get(crs.get(i))));
	}

	/**
	 * Removes most powerful of the {@link #spawners}.
	 */
	public void downgrade() {
		if (spawners.size() > MINIMUMSPAWNERS) {
			spawners.remove(spawners.size() - 1);
		}
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
		return true;
	}

	/** Starts a {@link Rush}. */
	static public void start() {
		if (Javelin.prompt(
				"Start a dungeon rush game?\n\nPress ENTER start or any other key to cancel...") == '\n') {
			throw new StartBattle(new Rush(get()));
		}
	}

	/**
	 * @return The Dungoen Rush location in the {@link World}.
	 */
	public static DungeonRush get() {
		return (DungeonRush) WorldActor.getall(DungeonRush.class).get(0);
	}
}
