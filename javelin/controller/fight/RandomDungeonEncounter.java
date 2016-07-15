package javelin.controller.fight;

import java.util.List;

import javelin.Javelin;
import javelin.controller.terrain.map.tyrant.Caves;
import javelin.model.unit.Combatant;

/**
 * Generates a harder battle than a {@link RandomEncounter}.
 * 
 * @author alex
 */
public class RandomDungeonEncounter extends RandomEncounter {
	public RandomDungeonEncounter() {
		meld = true;
		map = new Caves();
		/* TODO enable in Dungeon as well on 2.0 */
		bribe = false;
		hide = false;
	}

	@Override
	public int getel(int teamel) {
		return teamel + Javelin.randomdifficulty() + 1;
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return null;
	}
}
