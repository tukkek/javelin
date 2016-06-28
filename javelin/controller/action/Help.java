package javelin.controller.action;

import javelin.Javelin;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;

/**
 * Shows keyboard commands.
 * 
 * @author alex
 */
public class Help extends Action {

	public Help() {
		super("help", new String[] { "h", "?" });
		allowwhileburrowed = true;
	}

	@Override
	public boolean perform(Combatant hero, BattleMap m, Thing thing) {
		help(ActionMapping.actions);
		return false;
	}

	static public void help(ActionDescription[] actions) {
		String text =
				"These are all the available commands on this screen. Movement is also used to attack adjacent enemies.\n\n";
		for (final ActionDescription a : actions) {
			final String[] keys = a.getDescriptiveKeys();
			if (keys.length == 0) {
				continue;
			}
			boolean first = true;
			for (final String key : keys) {
				if (key.contains("arrow")) {
					continue;
				}
				if (first) {
					first = false;
				} else {
					text += " or ";
				}
				text += key;
			}
			text += ": " + a.getDescriptiveName() + "\n";
		}
		text += "\nKeep up-to-date with new releases at javelinrl.wordpress.com\nor come discuss the game at reddit.com/r/javelinrl !";
		Javelin.app.switchScreen(new InfoScreen(Game.getQuestapp(), text));
		Game.getInput();
		Javelin.app.switchScreen(BattleScreen.active);
	}

}
