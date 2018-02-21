package javelin.controller.generator.encounter;

import javelin.controller.db.reader.fields.Organization;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.RPG;

/**
 * @see Organization#init()
 */
public class EncounterPossibilities {
	/** Minimum quantity. */
	public int min;
	/** Maximum quantity. */
	public int max;
	/** Monster type. */
	public Monster m;
	/** Random monster group. */
	public String random;

	/**
	 * @return {@link #m} or a random element from {@link #random}.
	 */
	public Monster getmonster() {
		if (m != null) {
			return m;
		}
		String[] group = Organization.RANDOM.get(random);
		if (group == null) {
			throw new RuntimeException(
					"Unknown random monster group: " + random);
		}
		return Organization.monstersbyname.get(RPG.pick(group).toLowerCase());
	}
}