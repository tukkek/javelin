package javelin.model.unit.abilities;

import javelin.controller.upgrade.Spell;
import javelin.model.unit.CloneableList;

/**
 * Known spells.
 * 
 * @author alex
 */
public class Spells extends CloneableList<Spell> {
	public Spell has(Spell spell) {
		for (Object sp : this) {
			Spell s = (Spell) sp;
			if (s.equals(spell)) {
				return s;
			}
		}
		return null;
	}
}
