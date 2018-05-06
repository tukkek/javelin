package javelin.controller.fight;

import java.util.ArrayList;

import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Generates a harder battle than a {@link RandomEncounter}.
 *
 * @author alex
 */
public class RandomDungeonEncounter extends RandomEncounter {
	Dungeon dungeon;

	/** Constructor. */
	public RandomDungeonEncounter(Dungeon d) {
		this();
		dungeon = d;
	}

	protected RandomDungeonEncounter() {
		meld = true;
		map = Terrain.UNDERGROUND.getmaps().pick();
	}

	@Override
	public Integer getel(int teamel) {
		return dungeon.level + EncounterGenerator.getdifficulty();
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return null;
	}

	@Override
	public ArrayList<Terrain> getterrains() {
		ArrayList<Terrain> terrains = new ArrayList<Terrain>(1);
		terrains.add(Terrain.UNDERGROUND);
		return terrains;
	}
}
