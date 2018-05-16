package javelin.model.world.location.unique.minigame;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.action.world.minigame.EnterArena;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.unique.UniqueLocation;
import javelin.view.screen.WorldScreen;

/**
 * A battle arena minigamge based on MOBAs. Ideally a single match should last
 * between 30 to 90 minutes.
 *
 * @author alex
 */
public class Arena extends UniqueLocation {
	static final String DESCRIPTION = "The arena";

	/**
	 * Roster of permanent player units.
	 *
	 * TODO use as reward
	 */
	public Combatants gladiators = new Combatants();

	/** Constructor. */
	public Arena() {
		super(DESCRIPTION, DESCRIPTION, 0, 0);
	}

	@Override
	protected void generategarrison(int minlevel, int maxlevel) {
		// don't
	}

	@Override
	public List<Combatant> getcombatants() {
		return gladiators;
	}

	@Override
	public boolean interact() {
		if (!super.interact()) {
			return false;
		}
		new EnterArena().perform(WorldScreen.current);
		return true;

	}

	/**
	 * @return The Arena. A <code>null</code> result could indicate the game
	 *         hasn't yet been loaded or the world isn't generated.
	 */
	public static Arena get() {
		ArrayList<Actor> actors = World.getall(Arena.class);
		return actors.isEmpty() ? null : (Arena) actors.get(0);
	}

	@Override
	protected void generate() {
		while (x < 0 || Terrain.get(x, y).equals(Terrain.MARSH)
				|| Terrain.get(x, y).equals(Terrain.MOUNTAINS)
				|| Terrain.get(x, y).equals(Terrain.DESERT)) {
			super.generate();
		}
	}
}