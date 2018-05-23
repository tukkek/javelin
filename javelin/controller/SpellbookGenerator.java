package javelin.controller;

import java.util.TreeMap;

import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.abilities.spell.Spells;
import javelin.old.RPG;

public class SpellbookGenerator {
	static final TreeMap<Float, Spells> SPELLS = new TreeMap<Float, Spells>();

	static {
		for (final Spell s : Spell.SPELLS.values()) {
			Spells list = SPELLS.get(s.cr);
			if (list == null) {
				list = new Spells();
				SPELLS.put(s.cr, list);
			}
			list.add(s);
		}
	}

	public static void generate(Combatant combatant) {
		float crremaining = combatant.source.spellcr;
		for (Spell s : combatant.spells) {
			crremaining -= s.cr * s.perday;
		}
		distribution: while (crremaining > 0) {
			selection: for (float cost : SPELLS.descendingKeySet()) {
				Spell s = RPG.pick(SPELLS.get(cost));
				if (s.castinbattle && s.cr <= crremaining
						&& combatant.source.hd.count() >= s.casterlevel
						&& s.apply(combatant.clone().clonesource())) {
					Spell learned = combatant.spells.has(s);
					if (learned == null) {
						combatant.spells.add(s.clone());
					} else {
						if (learned.perday == 5) {
							continue selection;
						}
						learned.perday += 1;
					}
					crremaining -= s.cr;
					continue distribution;
				}
			}
			break;
		}
	}
}
