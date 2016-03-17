package javelin.controller.fight;

import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.Weather;
import javelin.controller.map.Map;
import javelin.controller.map.TyrantMap;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * Generates a harder battle than a {@link RandomEncounter}.
 * 
 * @author alex
 */
public class RandomDungeonEncounter implements Fight {
	@Override
	public int getel(JavelinApp javelinApp, int teamel) {
		return Javelin.randomdifficulty() + 1;
	}

	@Override
	public BattleScreen getscreen(JavelinApp javelinApp, BattleMap battlemap) {
		return new BattleScreen(javelinApp, battlemap, true);
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return null;
	}

	@Override
	public boolean meld() {
		return true;
	}

	@Override
	public Map getmap() {
		return new TyrantMap("caves", Weather.DRY);
	}

	@Override
	public boolean friendly() {
		return false;
	}

}
