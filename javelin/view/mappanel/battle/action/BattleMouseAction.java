package javelin.view.mappanel.battle.action;

import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.Tile;

public abstract class BattleMouseAction {
	public boolean clearoverlay = true;

	public abstract boolean validate(Combatant current, Combatant target,
			BattleState s);

	public abstract Runnable act(Combatant current, Combatant target,
			BattleState s);

	public abstract void onenter(Combatant current, Combatant target, Tile t,
			BattleState s);
}