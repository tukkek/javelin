package javelin.controller.fight.minigame;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.terrain.map.Map;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.screen.BattleScreen;

/**
 * A minigame is a battle totally independent from the normal game {@link Squad}
 * . It usually has differing objectives, mechanics and overall dynamics.
 * 
 * @author alex
 */
public abstract class Minigame extends Fight {
	/** Constructor. */
	public Minigame() {
		bribe = false;
		hide = false;
		map = Map.random();
	}

	@Override
	public int getel(int teamel) {
		throw new RuntimeException("#noautogenerate #minigame");
	}

	@Override
	abstract public boolean onend();

	@Override
	public void withdraw(Combatant combatant, BattleScreen screen) {
		if (Javelin.prompt("Do you want to abandon this match?\n\n"
				+ "Press ENTER to confirm, any other key to cancel...") == '\n') {
			throw new EndBattle();
		}
		Game.messagepanel.clear();
	}

	@Override
	public ArrayList<Item> getbag(Combatant combatant) {
		return new ArrayList<Item>();
	}
}
