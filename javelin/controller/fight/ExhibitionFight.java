package javelin.controller.fight;

import java.util.List;

import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.tournament.Exhibition;
import javelin.controller.terrain.map.Arena;
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
public class ExhibitionFight extends Fight {
	public ExhibitionFight() {
		map = new Arena();
		meld = true;
		friendly = true;
		hide = false;
		bribe = false;
	}

	@Override
	public List<Combatant> getmonsters(int teamel) {
		return null;
	}

	@Override
	public int getel(int teamel) {
		return teamel;
	}

	@Override
	public void checkEndBattle(BattleScreen screen) {
		super.checkEndBattle(screen);
		if (BattleMap.blueTeam.isEmpty()) {
			throw new EndBattle();
		}
	}

	@Override
	public void withdraw(Combatant combatant, BattleScreen screen) {
		PlanarFight.dontflee(screen);
	}
}