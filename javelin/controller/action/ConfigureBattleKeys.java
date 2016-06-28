package javelin.controller.action;

import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.frame.keys.BattleKeysScreen;
import tyrant.mikera.engine.Thing;

public class ConfigureBattleKeys extends Action {

	public ConfigureBattleKeys() {
		super("Configure keys", "K");
	}

	@Override
	public boolean perform(Combatant active, BattleMap m, Thing thing) {
		new BattleKeysScreen().show();
		ActionMapping.reset = true;
		return false;
	}

}
