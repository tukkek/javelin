package javelin.controller.exception;

import javelin.controller.fight.Fight;

public class StartBattle extends BattleEvent {

	public final Fight fight;

	public StartBattle(final Fight d) {
		fight = d;
	}
}
