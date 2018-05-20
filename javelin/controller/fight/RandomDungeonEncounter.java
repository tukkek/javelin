package javelin.controller.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
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
		Combatants encounter = RPG.pick(dungeon.encounters);
		if (encounter == null) {
			return null;
		}
		int el = ChallengeCalculator.calculateel(encounter);
		if (el - Squad.active.getel() <= Difficulty.VERYEASY) {
			return null;
		}
		return encounter.clone();
	}

	@Override
	public boolean avoid(List<Combatant> foes) {
		return foes == null || super.avoid(foes);
	}
}
