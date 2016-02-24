package javelin.controller.fight;

import java.util.List;

import javelin.JavelinApp;
import javelin.controller.map.Map;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.Lair;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.LairScreen;

/**
 * A moderate fight is teamel-4, a difficult fight teamel-2 (double the
 * difficulty). The difficulty here is bumped once more due to the last enemy
 * requiring only 50% hp to capture.
 * 
 * @see Lair
 * 
 * @author alex
 */
public class LairFight implements Fight {
	@Override
	public BattleScreen getscreen(final JavelinApp javelinApp,
			final BattleMap battlemap) {
		LairScreen dungeonScreen = new LairScreen(javelinApp, battlemap);
		return dungeonScreen;
	}

	@Override
	public int getel(final JavelinApp javelinApp, final int teamel) {
		return teamel - 1;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean friendly() {
		return false;
	}
}