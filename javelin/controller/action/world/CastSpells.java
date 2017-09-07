package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.view.screen.WorldScreen;

/**
 * Cast {@link Spell}s out of battle.
 * 
 * @see Spell#castoutofbattle
 * @author alex
 */
public class CastSpells extends WorldAction {
	/** Constructor. */
	public CastSpells() {
		super("Cast spells", new int[0], new String[] { "s" });
	}

	@Override
	public void perform(WorldScreen screen) {
		List<String> names = new ArrayList<String>();
		ArrayList<Combatant> casters = filtercasters(names);
		if (casters.isEmpty()) {
			Game.messagepanel.clear();
			Game.message("No peaceful spells to cast right now...", Delay.WAIT);
			return;
		}
		int choice = Javelin.choose("Who?", names, false, false);
		if (choice == -1) {
			Game.messagepanel.clear();
			return;
		}
		Combatant caster = casters.get(choice);
		List<Spell> spells = new ArrayList<Spell>(caster.spells);
		Spell s = selectspell(spells);
		if (s == null) {
			Game.messagepanel.clear();
			return;
		}
		Combatant target = null;
		if (s.castonallies) {
			int targetindex = selecttarget();
			if (targetindex < 0) {
				Game.messagepanel.clear();
				return;
			}
			target = Squad.active.members.get(targetindex);
		}
		if (!s.validate(caster, target)) {
			Game.message("Can't cast this spell right now.", Delay.BLOCK);
			return;
		}
		String message = s.castpeacefully(caster, target);
		if (message != null) {
			Javelin.message(message, false);
		}
		s.used += 1;
	}

	int selecttarget() {
		List<String> targets = new ArrayList<String>();
		for (Combatant m : Squad.active.members) {
			targets.add(m.source.toString());
		}
		int targetindex = Javelin.choose("Cast on...", targets, false, false);
		return targetindex;
	}

	private Spell selectspell(List<Spell> spells) {
		ArrayList<String> spellnames = listspells(spells);
		if (spellnames.size() == 0) {
			Game.messagepanel.clear();
			Game.message("All spells already cast! Rest to regain them.",
					Delay.BLOCK);
			return null;
		}
		int input = Javelin.choose("Which spell?", spellnames, false, false);
		if (input == -1) {
			return null;
		}
		String name = spellnames.get(input);
		for (Spell s : spells) {
			if (s.toString().equals(name)) {
				return s;
			}
		}
		throw new RuntimeException("Should have caught spell name");
	}

	ArrayList<Combatant> filtercasters(List<String> names) {
		ArrayList<Combatant> casters = new ArrayList<Combatant>(
				Squad.active.members);
		for (Combatant m : new ArrayList<Combatant>(casters)) {
			if (listspells(new ArrayList<Spell>(m.spells)).size() == 0) {
				casters.remove(m);
			} else {
				names.add(m.source.toString());
			}
		}
		return casters;
	}

	ArrayList<String> listspells(List<Spell> spells) {
		ArrayList<String> spellnames = new ArrayList<String>();
		for (Spell s : spells) {
			if (!s.exhausted() && s.castoutofbattle) {
				spellnames.add(s.toString());
			}
		}
		return spellnames;
	}
}
