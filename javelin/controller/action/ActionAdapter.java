package javelin.controller.action;

import javelin.controller.action.world.WorldAction;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import tyrant.mikera.engine.Thing;

/**
 * Used to create an in-battle {@link Action} from a {@link WorldAction}.
 * 
 * TODO at this point we're probably close enough to unify {@link WorldAction}
 * and {@link Action}.
 * 
 * @author alex
 */
public class ActionAdapter extends Action {
	SimpleAction action;

	public ActionAdapter(SimpleAction a) {
		super(a.getname());
		action = a;
		if (a.getcodes() != null) {
			keycodes = a.getcodes();
		}
		if (a.getkeys() != null) {
			keys = a.getkeys();
		}
	}

	@Override
	public boolean perform(Combatant hero, BattleMap m, Thing thing) {
		action.perform();
		return false;
	}
}
