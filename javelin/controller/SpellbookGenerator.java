package javelin.controller;

import java.util.TreeMap;

import javelin.controller.upgrade.Spell;
import javelin.model.spell.DominateMonster;
import javelin.model.unit.Combatant;
import javelin.model.unit.abilities.Spells;
import tyrant.mikera.engine.RPG;

public class SpellbookGenerator {
	static final TreeMap<Float, Spells> SPELLS = new TreeMap<Float, Spells>();

	static {
		for (final Spell s : Spell.SPELLS.values()) {
			if (s.equals(DominateMonster.singleton)) {
				/*
				 * TODO make sure it returns a Squad member by the logical end
				 * of battle
				 */
				continue;
			}
			Spells list = SPELLS.get(s.cr);
			if (list == null) {
				list = new Spells();
				SPELLS.put(s.cr, list);
			}
			list.add(s);
		}
	}

	public static void generate(Combatant combatant) {
		combatant.source = combatant.source.clone();
		float crremaining = combatant.source.spellcr;
		for (Spell s : combatant.source.spells) {
			crremaining -= s.cr;
		}
		// float crused = 0;
		distribution: while (crremaining > 0) {
			selection: for (float cost : SPELLS.descendingKeySet()) {
				Spell s = RPG.pick(SPELLS.get(cost));
				if (s.cr <= crremaining
						&& combatant.source.hd.count() >= s.casterlevel
						&& s.apply(combatant.clonedeeply())) {
					Spell learned = combatant.spells.has(s);
					if (learned == null) {
						combatant.spells.add(s.clone());
					} else {
						if (learned.perday == 5) {
							continue selection;
						}
						learned.perday += 1;
					}
					// crused += s.cr;
					crremaining -= s.cr;
					continue distribution;
				}
			}
			break;
		}
		// combatant.source.spellcr = crused;
	}
}
