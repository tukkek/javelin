package javelin.view.frame.keys;

import java.util.ArrayList;

import javelin.controller.action.Action;
import javelin.controller.action.ActionAdapter;
import javelin.controller.action.ActionDescription;
import javelin.controller.action.ActionMapping;
import javelin.view.KeysScreen;

/**
 * Configures battle keys.
 * 
 * @author alex
 */
public class BattleKeysScreen extends KeysScreen {
	/** Constructor. */
	public BattleKeysScreen() {
		super("Battle keys", "keys.battle");
	}

	@Override
	public ArrayList<ActionDescription> getactions() {
		ArrayList<ActionDescription> list =
				new ArrayList<ActionDescription>(ActionMapping.actions.length);
		for (Action c : ActionMapping.actions) {
			if (!(c instanceof ActionAdapter) && c.getMainKey() != null) {
				list.add(c);
			}
		}
		return list;
	}

}
