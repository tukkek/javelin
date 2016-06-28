package javelin.controller.action.world.improvement;

import javelin.controller.action.world.Work;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.Improvement;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Inn;

/**
 * Builds an {@link Inn}.
 * 
 * @see Work
 * @author alex
 */
public class BuildInn extends Improvement {
	/** Constructor. */
	public BuildInn(String name, double price, Character key) {
		super(name, price, key);
	}

	@Override
	public Location done(int x, int y) {
		removeworker();
		Squad.active.remove(null);
		Location i = new Inn();
		i.setlocation(x, y);
		return i;
	}

	static void removeworker() {
		Combatant worker = null;
		for (Combatant c : Squad.active.members) {
			if (c.source.name.equals("Worker")) {
				worker = c;
				break;
			}
		}
		if (worker != null) {
			Squad.active.remove(worker);
		}
	}
}