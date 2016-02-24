package javelin.controller.exception.battle;

import javelin.controller.fight.Fight;

/**
 * A {@link Fight} has started.
 * 
 * @author alex
 */
public class StartBattle extends BattleEvent {

	public final Fight fight;

	public StartBattle(final Fight d) {
		fight = d;
	}
}
