package javelin.model.item.relic;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.WorldScreen;

/**
 * Terraforms surroundings (3x3?) except to/from water.
 * 
 * @author alex
 */
public class Map extends Relic {
	static final int RADIUS = 2;

	/** Constructor. */
	public Map() {
		super("Map of Creation", Realm.FIRE);
		usedinbattle = false;
		usedoutofbattle = true;
	}

	@Override
	protected boolean activate(Combatant user) {
		if (Dungeon.active != null) {
			Javelin.app.switchScreen(WorldScreen.active);
			Javelin.message(
					"You draw on the map but nothing happens. Try it outside next time!",
					false);
			return true;
		}
		ArrayList<Terrain> terrains =
				new ArrayList<Terrain>(Terrain.ALL.length - 1);
		for (Terrain t : Terrain.ALL) {
			if (!t.equals(Terrain.WATER)) {
				terrains.add(t);
			}
		}
		int i = Javelin.choose("Transform the surrounding terrain into what?",
				terrains, true, false);
		if (i == -1) {
			return false;
		}
		Terrain terrain = terrains.get(i);
		for (int x = Squad.active.x - RADIUS; x <= Squad.active.x
				+ RADIUS; x++) {
			for (int y = Squad.active.y - RADIUS; y <= Squad.active.y
					+ RADIUS; y++) {
				if (World.validatecoordinate(x, y)
						&& !Terrain.get(x, y).equals(Terrain.WATER)) {
					World.getseed().map[x][y] = terrain;
				}
			}
		}
		return true;
	}
}
