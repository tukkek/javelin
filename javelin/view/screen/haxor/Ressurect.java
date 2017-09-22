package javelin.view.screen.haxor;

import javelin.Javelin;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;

/**
 * One-time offer to ressurect a fallen friendly {@link Combatant}.
 */
public class Ressurect extends Hax {
	/** Constructor. */
	public Ressurect(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		if (EndBattle.lastkilled == null) {
			s.text += "\nNo ally has died yet.";
			Javelin.app.switchScreen(s);
			return false;
		}
		EndBattle.lastkilled.hp = EndBattle.lastkilled.maxhp;
		Squad.active.add(EndBattle.lastkilled);
		return true;
	}
}