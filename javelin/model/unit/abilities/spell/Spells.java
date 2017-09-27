package javelin.model.unit.abilities.spell;

import javelin.model.unit.CloneableList;
import javelin.model.unit.attack.Combatant;

/**
 * Known spells for a {@link Combatant}.
 * 
 * @author alex
 */
public class Spells extends CloneableList<Spell> {
	/**
	 * @param spell
	 *            Given a spell class...
	 * @return the instance of such spell or <code>null</code> if none is found.
	 */
	public Spell has(Spell spell) {
		for (Object sp : this) {
			Spell s = (Spell) sp;
			if (s.equals(spell)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * @return The total number of spell casts per day for all current
	 *         {@link Spell}s.
	 * @see Spell#perday
	 */
	public int count() {
		int sum = 0;
		for (Spell s : this) {
			sum += s.perday;
		}
		return sum;
	}
}