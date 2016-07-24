package javelin.controller.action;

import javelin.model.unit.Combatant;
import javelin.view.frame.keys.BattleKeysScreen;

public class ConfigureBattleKeys extends Action {

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
