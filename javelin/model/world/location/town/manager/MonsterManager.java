package javelin.model.world.location.town.manager;

import java.util.ArrayList;

import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.research.Research;

/**
 * Manager for hostile towns.
 * 
 * Note that monsters have a different drawing pool.
 * 
 * @see Town#ishostile()
 * @see Research#draw(Town)
 * @author alex
 */
public class MonsterManager extends TownManager {
	@Override
	protected Research picknexttask(Town t) {
		if (t.size < 3) {
			return t.research.hand[0];
		}
		ArrayList<Research> options = new ArrayList<Research>();
		for (Research r : t.research.hand) {
			if (r == null || !r.aiable) {
				continue;
			}
			options.add(r);
		}
		options.add(t.research.hand[0]);
		return TownManager.pickcheap(options);
	}

}
