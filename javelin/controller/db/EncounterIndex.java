package javelin.controller.db;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.controller.encounter.Encounter;

/**
 * Mapping of {@link Encounter}s by encounter level.
 * 
 * @author alex
 */
public class EncounterIndex extends TreeMap<Integer, List<Encounter>> {

	/**
	 * @param e
	 *            Register this with the given challenge rating.
	 */
	public void put(int cr, Encounter e) {
		List<Encounter> tier = get(cr);
		if (tier == null) {
			tier = new ArrayList<Encounter>();
			put(cr, tier);
		}
		tier.add(e);
	}

}
