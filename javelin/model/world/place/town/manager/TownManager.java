package javelin.model.world.place.town.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.model.world.place.town.Town;
import javelin.model.world.place.town.research.Research;
import tyrant.mikera.engine.RPG;

/**
 * Decides how to automatically build a town.
 * 
 * @see Town#work()
 * @author alex
 */
public abstract class TownManager {

	/**
	 * @param t
	 *            Town for which to plan and execute next buils on.
	 */
	public void manage(Town t) {
		if (t.nexttask == null) {
			t.nexttask = picknexttask(t);
		}
		if (t.nexttask.price <= t.labor) {
			t.nexttask.finish(t, null);
			t.nexttask = null;
		}
	}

	/**
	 * @param t
	 *            Given a town...
	 * @return decide what to build next.
	 */
	abstract protected Research picknexttask(Town t);

	/**
	 * @return A random research pick whose chance of being selected is larger
	 *         when it is cheaper.
	 */
	public static Research pickcheap(ArrayList<Research> options) {
		options = new ArrayList<Research>(options);
		while (options.contains(null)) {
			options.remove(null);
		}
		Collections.sort(options, new Comparator<Research>() {
			@Override
			public int compare(Research o1, Research o2) {
				return Math.round(Math.round(o1.price - o2.price));
			}
		});
		int totalprice = 0;
		for (Research r : options) {
			totalprice += r.price;
		}
		double roll = RPG.r(0, totalprice);
		int forward = 0;
		for (int i = options.size() - 1; i >= 0; i--) {
			roll -= options.get(i).price;
			if (roll <= 0) {
				return options.get(forward);
			}
			forward += 1;
		}
		assert false;
		return null;
	}

}
