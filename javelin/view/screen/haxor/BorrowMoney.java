package javelin.view.screen.haxor;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.unique.Haxor;

/**
 * Will lend money to a {@link Squad} once but will get it back on a future
 * visit.
 * 
 * @see Haxor#borrowed
 * @author alex
 */
public class BorrowMoney extends Hax {
	/**
	 * See {@link Hax#Hax(String, double, boolean)}.
	 * 
	 * @param keyp
	 */
	public BorrowMoney(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		Haxor.singleton.borrowed = Haxor.borrow();
		Squad.active.gold += Haxor.singleton.borrowed;
		s.print("Don't come back until you can pay me these $"
				+ Haxor.singleton.borrowed + "!");
		s.feedback();
		return true;
	}

}
