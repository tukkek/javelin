package javelin.model.unit;

import java.util.ArrayList;

import javelin.controller.upgrade.Spell;

public class Spells extends ArrayList<Spell> {
	@Override
	public Spells clone() {
		final Spells clone = (Spells) super.clone();
		for (int i = 0; i < size(); i++) {
			clone.set(i, get(i).clone());
		}
		return clone;
	}

	public Spell has(Spell spell) {
		for (Spell s : this) {
			if (s.getClass() == spell.getClass()) {
				return s;
			}
		}
		return null;
	}
}
