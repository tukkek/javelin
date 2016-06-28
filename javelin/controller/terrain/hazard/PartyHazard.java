package javelin.controller.terrain.hazard;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

/**
 * This is a helper class for hazard effects that affect all squad members.
 * 
 * @see Terrain#gethazards(int, boolean)
 * @author alex
 */
public abstract class PartyHazard extends Hazard {
	/**
	 * @return <code>true</code> if the character is able to ignore the penalty.
	 */
	protected abstract boolean save(int hoursellapsed, Combatant c);

	/**
	 * Damages/penalizes a {@link Combatant} who failed it's save. Ideally
	 * should never outright kill a character since it's no fun to lose an unit
	 * passively (non-lethal damage). Also makes it a bit easier to code,
	 * without having to worry about {@link Ressurect}ions and such.
	 * 
	 * @param c
	 *            Penalizes/damages this character.
	 * @return description of what happen. Should contain the character's name.
	 */
	protected abstract String affect(Combatant c, int hoursellapsed);

	@Override
	public void hazard(int hoursellapsed) {
		String output = "";
		for (Combatant c : Squad.active.members) {
			if (!save(hoursellapsed, c)) {
				output += affect(c, hoursellapsed) + ", ";
			}
		}
		if (!output.isEmpty()) {
			Javelin.message(output.substring(0, output.length() - 2) + "!",
					false);
		}
	}

}