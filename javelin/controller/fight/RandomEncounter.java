package javelin.controller.fight;

import java.util.List;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.RPG;

/**
 * Fight that happens on the overworld map.
 * 
 * @author alex
 */
public class RandomEncounter extends Fight {
	@Override
	public int getel(int teamel) {
		int difficulty = Javelin.randomdifficulty() + Javelin.difficulty();
		return teamel + Terrain.current().cap(difficulty);
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return null;
	}

	/**
	 * @param d
	 *            % chance of starting a battle.
	 * @throws StartBattle
	 */
	static public void encounter(double d) {
		if (RPG.random() < d && !Preferences.DEBUGDISABLECOMBAT) {
			throw new StartBattle(Dungeon.active == null ? new RandomEncounter()
					: new RandomDungeonEncounter());
		}
	}
}