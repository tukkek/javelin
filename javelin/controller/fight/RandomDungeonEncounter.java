package javelin.controller.fight;

import java.util.ArrayList;

import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.RPG;

/**
 * Generates a harder battle than a {@link RandomEncounter}.
 *
 * @author alex
 */
public class RandomDungeonEncounter extends RandomEncounter {
	Dungeon dungeon;

	/** Constructor. */
	public RandomDungeonEncounter(Dungeon d) {
		dungeon = d;
		meld = true;
		map = Terrain.UNDERGROUND.getmaps().pick();
	}

	@Override
	public ArrayList<Combatant> generate() {
		return RPG.pick(dungeon.encounters).clone();
	}
}
