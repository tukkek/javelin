package javelin.controller.fight;

import java.util.List;

import javelin.JavelinApp;
import javelin.controller.map.Arena;
import javelin.controller.map.DndMap;
import javelin.controller.map.Map;
import javelin.controller.tournament.Exhibition;
import javelin.controller.tournament.ExhibitionScreen;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * Tournament event.
 * 
 * @see Exhibition
 * 
 * @author alex
 */
public class ExhibitionFight implements Fight {
	@Override
	public boolean meld() {
		return true;
	}

	@Override
	public BattleScreen getscreen(JavelinApp javelinApp, BattleMap battlemap) {
		return new ExhibitionScreen(javelinApp, battlemap, true);
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return null;
	}

	@Override
	public int getel(JavelinApp javelinApp, int teamel) {
		return teamel;
	}

	@Override
	public Map getmap() {
		return new Arena(DndMap.SIZE, DndMap.SIZE);
	}

	@Override
	public boolean friendly() {
		return true;
	}

	@Override
	public boolean rewardgold() {
		return true;
	}

	@Override
	public boolean hide() {
		return false;
	}

	@Override
	public void bribe() {
		throw new RuntimeException("Cannot bribe an Exhibition! #exhibitionf");
	}

	@Override
	public boolean canbribe() {
		return false;
	}
}