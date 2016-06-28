package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.screen.Option;

/**
 * Lets the player convert a {@link Combatant} into a 1 unit growth of
 * {@link Town#size}.
 * 
 * @author alex
 */
public class SettleOption extends Option {
	/** Constructor. */
	public SettleOption() {
		super("Settle", 0, 'S');
	}

	/** TODO make it hierarchy method */
	public static boolean retire(Town town) {
		List<Combatant> retirees = new ArrayList<Combatant>();
		for (Combatant c : Squad.active.members) {
			if (!c.mercenary) {
				retirees.add(c);
			}
		}
		if (retirees.isEmpty()) {
			return false;
		}
		int choice = Javelin.choose(
				"Which member should retire and become local labor?", retirees,
				true, false);
		if (choice < 0) {
			return false;
		}
		Squad.active.remove(retirees.get(choice));
		town.size += 1;
		return true;
	}
}
