package javelin.controller.fight;

import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.map.Map;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.place.Dungeon;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.tyrant.Tile;

/**
 * Fight that happens on the overworld map.
 * 
 * @author alex
 */
public class RandomEncounter implements Fight {
	@Override
	public BattleScreen getscreen(final JavelinApp javelinApp,
			final BattleMap battlemap) {
		return new BattleScreen(javelinApp, battlemap, true);
	}

	@Override
	public int getel(final JavelinApp app, int teamel) {
		int difficulty = Javelin.randomdifficulty() + Javelin.difficulty();
		return teamel + cap(difficulty, Javelin.terrain());
	}

	private int cap(int difficulty, int tile) {
		if (tile == Tile.PLAINS && difficulty > -4) {
			return -4;
		}
		if (tile == Tile.FORESTS && difficulty > -3) {
			return -3;
		}
		if (tile == Tile.HILLS && difficulty > -2) {
			return -2;
		}
		if (tile == Tile.GUNK && difficulty > -1) {
			return -1;
		}
		return difficulty;
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return null;
	}

	@Override
	public boolean meld() {
		return Dungeon.active != null;
	}

	@Override
	public Map getmap() {
		return null;
	}

	@Override
	public boolean friendly() {
		return false;
	}
}