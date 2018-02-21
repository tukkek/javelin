package javelin.view.mappanel.battle.action;

import javelin.model.state.BattleState;
import javelin.model.unit.attack.Combatant;
import javelin.view.mappanel.Tile;

public abstract class BattleMouseAction {
	public boolean clearoverlay = true;

	public abstract boolean determine(Combatant current, Combatant target,
			BattleState s);

	public abstract void act(Combatant current, Combatant target,
			BattleState s);

	public abstract void onenter(Combatant current, Combatant target, Tile t,
			BattleState s);
}