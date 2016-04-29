package javelin.view.screen.haxor;

import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.place.unique.Haxor;

/**
 * Will lend money to a {@link Squad} once but will get it back on a future
 * visit.
 * 
 * @see Haxor#borrowed
 * @author alex
 */
public class BorrowMoney extends Hax {
	/** See {@link Hax#Hax(String, double, boolean)}. */
	public BorrowMoney(String name, double price, boolean requirestargetp) {
		super(name, price, requirestargetp);
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
