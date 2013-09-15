package javelin.controller;

import java.util.TreeMap;

import javelin.Javelin;
import javelin.controller.upgrade.Spell;
import javelin.model.spell.BearsEndurance;
import javelin.model.spell.BullsStrength;
import javelin.model.spell.CatsGrace;
import javelin.model.spell.DayLight;
import javelin.model.spell.DeeperDarkness;
import javelin.model.spell.Heroism;
import javelin.model.spell.HoldMonster;
import javelin.model.spell.SlayLiving;
import javelin.model.spell.wounds.CureCriticalWounds;
import javelin.model.spell.wounds.CureModerateWounds;
import javelin.model.spell.wounds.CureSeriousWounds;
import javelin.model.spell.wounds.InflictCriticalWounds;
import javelin.model.spell.wounds.InflictModerateWounds;
import javelin.model.spell.wounds.InflictSeriousWounds;
import javelin.model.unit.Combatant;
import javelin.model.unit.Spells;
import tyrant.mikera.engine.RPG;

public class SpellbookGenerator {
	/**
	 * TODO can't have all spells by default: Dominate Monster for example would
	 * need to be properly adapted to work on the red team.
	 */
	static final TreeMap<Float, Spells> SPELLS = new TreeMap<Float, Spells>();

	private static void add(Spell s) {
		if (Javelin.DEBUG) {
			System.out.println("#croutput " + s.name + " " + s.cr);
		}
		Spells list = SPELLS.get(s.cr);
		if (list == null) {
			list = new Spells();
			SPELLS.put(s.cr, list);
		}
		list.add(s);
	}

	static {
		add(new CureModerateWounds(""));
		add(new CureCriticalWounds(""));
		add(new InflictModerateWounds(""));
		add(new InflictSeriousWounds(""));
		add(new InflictCriticalWounds(""));
		add(new CatsGrace(""));
		add(new CureSeriousWounds(""));
		add(new DayLight(""));
		add(new Heroism(""));
		add(new DeeperDarkness(""));
		add(new SlayLiving(""));
		add(new HoldMonster(""));
		add(new BullsStrength(""));
		add(new BearsEndurance(""));
	}

	public static void generate(Combatant combatant) {
		float crremaining = combatant.source.spellcr;
		float crused = 0;
		distribution: while (crremaining > 0) {
			selection: for (float cost : SPELLS.descendingKeySet()) {
				Spell s = RPG.pick(SPELLS.get(cost));
				if (s.cr <= crremaining) {
					Spell learned = combatant.spells.has(s);
					if (learned == null) {
						combatant.spells.add(s.clone());
					} else {
						if (learned.perday == 5) {
							continue selection;
						}
						learned.perday += 1;
					}
					crused += s.cr;
					crremaining -= s.cr;
					continue distribution;

				}
			}
			break;
		}
		combatant.source.spellcr = crused;
	}
}
