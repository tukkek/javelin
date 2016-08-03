package javelin.controller.action;

import javelin.model.unit.Combatant;
import javelin.view.frame.keys.BattleKeysScreen;

/**
 * @see BattleKeysScreen
 * @author alex
 */
public class ConfigureBattleKeys extends Action {
	/** Constructor. */
	public ConfigureBattleKeys() {
		super("Configure keys", "K");
	}

	@Override
	public boolean perform(Combatant active) {
		new BattleKeysScreen().show();
		ActionMapping.reset = true;
		return false;
	}

}
