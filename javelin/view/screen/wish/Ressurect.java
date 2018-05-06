package javelin.view.screen.wish;

import javelin.Javelin;
import javelin.controller.exception.battle.EndBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

/**
 * One-time offer to ressurect a fallen friendly {@link Combatant}.
 */
public class Ressurect extends Wish {
	/**
	 * Constructor.
	 * 
	 * @param haxorScreen
	 */
	public Ressurect(String name, Character keyp, double price,
			boolean requirestargetp, WishScreen haxorScreen) {
		super(name, keyp, price, requirestargetp, haxorScreen);
	}

	@Override
	protected boolean wish(Combatant target) {
		if (EndBattle.lastkilled == null) {
			screen.text += "\nNo ally has died yet.";
			Javelin.app.switchScreen(screen);
			return false;
		}
		EndBattle.lastkilled.hp = EndBattle.lastkilled.maxhp;
		Squad.active.add(EndBattle.lastkilled);
		return true;
	}
}