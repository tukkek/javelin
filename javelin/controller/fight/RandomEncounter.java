package javelin.controller.fight;

import java.util.List;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.model.BattleMap;
import javelin.model.unit.Monster;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Tile;

public class RandomEncounter implements Fight {
	@Override
	public BattleScreen getscreen(final JavelinApp javelinApp,
			final BattleMap battlemap) {
		return new BattleScreen(javelinApp, battlemap);
	}

	@Override
	public int getel(final JavelinApp app, final int teamel) {
		int tile = JavelinApp.overviewmap.getTile(Game.hero().x, Game.hero().y);
		int difficulty = JavelinApp.randomdifficulty()
				+ Javelin.difficulty(tile);
		return teamel + cap(difficulty, tile);
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
	public List<Monster> getmonsters(int teamel) {
		return null;
	}
}