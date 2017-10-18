package javelin.controller.fight;

import java.util.ArrayList;

import javelin.JavelinApp;
import javelin.controller.db.Preferences;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.attack.Combatant;
import tyrant.mikera.engine.RPG;

/**
 * Fight that happens on the overworld map.
 * 
 * @author alex
 */
public class RandomEncounter extends Fight {
	@Override
	public Integer getel(int teamel) {
		return Terrain.current().getel(teamel);
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		return null;
	}

	/**
	 * @param d
	 *            % chance of starting a battle.
	 * @throws StartBattle
	 */
	static public void encounter(double d) {
		if (RPG.random() < d && !Preferences.DEBUGDISABLECOMBAT) {
			Fight f = JavelinApp.context.encounter();
			if (f != null) {
				throw new StartBattle(f);
			}
		}
	}

	static public StartBattle encountersafe(double d) {
		try {
			encounter(d);
			return null;
		} catch (StartBattle e) {
			return e;
		}
	}
}