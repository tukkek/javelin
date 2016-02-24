package javelin.controller.action;

import javelin.controller.action.world.WorldAction;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.Thing;

/**
 * Used to create an in-battle {@link Action} from a {@link WorldAction}.
 * 
 * @author alex
 */
public class ActionAdapter extends Action {
	SimpleAction action;

	public ActionAdapter(SimpleAction a) {
		super(a.getname(), a.getkeys());
		action = a;
	}

	@Override
	public boolean perform(Combatant hero, BattleMap m, Thing thing) {
		action.perform();
		return false;
	}
}
