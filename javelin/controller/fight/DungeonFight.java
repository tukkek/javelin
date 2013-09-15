package javelin.controller.fight;

import java.util.List;

import javelin.JavelinApp;
import javelin.model.BattleMap;
import javelin.model.unit.Monster;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.DungeonScreen;

/**
 * A moderate fight is teamel-4, a difficult fight tealem-2 (double the
 * difficulty). The difficulty here is bumped once more due to the last enemy
 * requiring only 50% hp to capture.
 * 
 * @author alex
 */
public class DungeonFight implements Fight {
	// private Monster capture;

	@Override
	public BattleScreen getscreen(final JavelinApp javelinApp,
			final BattleMap battlemap) {
		DungeonScreen dungeonScreen = new DungeonScreen(javelinApp, battlemap);
		// dungeonScreen.capture = capture;
		return dungeonScreen;
	}

	@Override
	public int getel(final JavelinApp javelinApp, final int teamel) {
		return teamel - 1;
	}

	@Override
	public List<Monster> getmonsters(int teamel) {
		// float[] crs = eltocr(teamel);
		// float cr = crs[RPG.r(0, crs.length - 1)];
		// for (Float tier : Javelin.MONSTERS.keySet()) {
		// if (tier >= cr) {
		// ArrayList<Monster> list = new ArrayList<Monster>();
		// capture = RPG.pick(Javelin.MONSTERS.get(tier));
		// list.add(capture);
		// return list;
		// }
		// }
		// throw new RuntimeException(
		// "No monster strong enough for lair! Game over?");
		return null;
	}

}