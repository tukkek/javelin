package javelin.controller.fight;

import java.util.ArrayList;

import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.attack.Combatant;

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
	public Integer getel(int teamel) {
		return teamel + EncounterGenerator.getdifficulty() + 1;
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return null;
	}
}
