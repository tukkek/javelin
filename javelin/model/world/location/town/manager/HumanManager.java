package javelin.model.world.location.town.manager;

import java.util.ArrayList;

import javelin.model.unit.transport.Transport;
import javelin.model.world.location.town.Accommodations;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Research;

/**
 * Builds native upgrades then native items. Later selects randomly. Tries to
 * grow meanwhile.
 * 
 * @see TownManager#pickcheap(ArrayList)
 * @author alex
 */
public class HumanManager extends TownManager {
	@Override
	protected Research picknexttask(Town t) {
		if ((t.lodging.equals(Accommodations.LODGE) && t.size >= 5)
				|| (t.lodging.equals(Accommodations.HOTEL) && t.size >= 15)) {
			return t.research.hand[7];
		}
		if ((t.transport == null && t.size >= 5)
				|| (Transport.CARRIAGE.equals(t.transport) && t.size >= 15)) {
			return t.research.hand[8];
		}
		ArrayList<Research> options = new ArrayList<Research>();
		options.add(t.research.hand[0]);
		if (t.research.hand[1] != null) {
			options.add(t.research.hand[1]);
		} else if (t.research.hand[3] != null) {
			options.add(t.research.hand[3]);
		} else {
			options.add(t.research.hand[2]);
			options.add(t.research.hand[4]);
			options.add(t.research.hand[5]);
			if (t.research.hand[6] != null && t.research.hand[6].aiable) {
				options.add(t.research.hand[6]);
			}
			while (options.contains(null)) {
				options.remove(null);
			}
		}
		return pickcheap(options);
	}

}
