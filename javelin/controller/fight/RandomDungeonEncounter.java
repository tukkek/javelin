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
	/**
	 * Adds this to {@link EncounterGenerator#getdifficulty()}.
	 */
	static final int ELMODIFIER = +1;

	/** Constructor. */
	public RandomDungeonEncounter() {
		meld = true;
		map = Terrain.UNDERGROUND.getmaps().pick();
	}

	@Override
	public Integer getel(int teamel) {
		return teamel + EncounterGenerator.getdifficulty() + ELMODIFIER;
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
