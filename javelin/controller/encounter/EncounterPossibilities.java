package javelin.controller.encounter;

import javelin.controller.db.reader.fields.Organization;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.RPG;

/**
 * @see Organization#process()
 */
public class EncounterPossibilities {
	public int min;
	public int max;
	public Monster m;
	public String random;

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