package javelin.controller.wish;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

/**
 * One-time offer to ressurect a fallen friendly {@link Combatant}.
 */
public class Ressurect extends Wish {
	/**
	 * Holds last killed allied non-mercenary, non-summoned {@link Combatant}.
	 */
	public static Combatant dead = null;

	/** Constructor. */
	public Ressurect(Character keyp, WishScreen s) {
		super("ressurect last fallen ally", keyp, 1, false, s);
		if (dead != null) {
			name += " (" + dead + ")";
			wishprice = Math.max(1, Math.round(dead.source.cr));
			price = wishprice;
		}
	}

	@Override
	boolean wish(Combatant target) {
		if (dead == null) {
			screen.print(screen.text + "\n"
					+ "No non-mercenary ally to ressurect...");
			return false;
		}
		dead.hp = dead.maxhp;
		Squad.active.add(dead);
		dead = null;
		return true;
	}

	@Override
	String validate() {
		if (dead == null) {
			return "No one to ressurect...";
		}
		return super.validate();
	}
}