package javelin.controller.fight;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;

/**
 * Generates a harder battle than a {@link RandomEncounter}.
 * 
 * @author alex
 */
public class RandomDungeonEncounter extends RandomEncounter {
	/** Constructor. */
	public RandomDungeonEncounter() {
		meld = true;
		map = Terrain.UNDERGROUND.getmaps().pick();
	}

	@Override
	public int getel(int teamel) {
		return teamel + Javelin.randomdifficulty() + 1;
	}

	@Override
	public ArrayList<Combatant> getmonsters(int teamel) {
		return null;
	}
}
