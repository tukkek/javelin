package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.NotPeaceful;
import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

public class CastSpells extends WorldAction {
	public CastSpells() {
		super("Cast spells", new int[0], new String[] { "s" });
	}

	@Override
	public void perform(WorldScreen screen) {
		List<String> names = new ArrayList<String>();
		ArrayList<Combatant> casters = filtercasters(names);
		if (casters.isEmpty()) {
			return;
		}
		int choice = choose("Who?", names, false, false);
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
		int targetindex = selecttarget();
		if (targetindex < 0) {
			Game.messagepanel.clear();
			return;
		}
		try {
			String message = s.castpeacefully(caster,
					Squad.active.members.get(targetindex));
			if (message != null) {
				Game.message(message, null, Delay.BLOCK);
			}
		} catch (NotPeaceful e) {
			throw new RuntimeException(
					"Should have been caught in CastSpells#listspells. See Spell#ispeaceful");
		}
		s.used += 1;
	}

	public int selecttarget() {
		List<String> targets = new ArrayList<String>();
		for (Combatant m : Squad.active.members) {
			targets.add(m.source.customName);
		}
		int targetindex = choose("Cast on...", targets, false, false);
		return targetindex;
	}

	private Spell selectspell(List<Spell> spells) {
		ArrayList<String> spellnames = listspells(spells);
		if (spellnames.size() == 0) {
			Game.messagepanel.clear();
			Game.message("All spells already cast! Rest to regain them.", null,
					Delay.BLOCK);
			return null;
		}
		int input = choose("Which spell?", spellnames, false, false);
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

	public ArrayList<Combatant> filtercasters(List<String> names) {
		ArrayList<Combatant> casters =
				new ArrayList<Combatant>(Squad.active.members);
		for (Combatant m : new ArrayList<Combatant>(casters)) {
			if (listspells(new ArrayList<Spell>(m.spells)).size() == 0) {
				casters.remove(m);
			} else {
				names.add(m.source.customName);
			}
		}
		return casters;
	}

	public ArrayList<String> listspells(List<Spell> spells) {
		ArrayList<String> spellnames = new ArrayList<String>();
		for (Spell s : spells) {
			if (!s.exhausted() && s.ispeaceful) {
				spellnames.add(s.toString());
			}
		}
		return spellnames;
	}

	/**
	 * Utility function for user-input selection.
	 * 
	 * @param output
	 *            Text to show the user.
	 * @param names
	 *            Will show each's {@link Object#toString()} as an option.
	 * @param fullscreen
	 *            <code>true</code> to open in a new screen. Otherwise uses the
	 *            message panel.
	 * @param forceselection
	 *            If <code>false</code> will allow the user to abort the
	 *            operation.
	 * @return The index of the selected element or -1 if aborted.
	 */
	static public int choose(String output, List<?> names, boolean fullscreen,
			boolean forceselection) {
		if (!forceselection) {
			output += " (q to quit)";
		}
		output += " \n\n";
		ArrayList<Object> options = new ArrayList<Object>();
		int i = 1;
		for (Object o : names) {
			String name = o.toString();
			options.add(name);
			output += "[" + i + "] " + name + "\n";
			i += 1;
		}
		if (fullscreen) {
			Javelin.app.switchScreen(new InfoScreen(output));
		} else {
			Game.messagepanel.clear();
			Game.message(output, null, Delay.NONE);
		}
		while (true) {
			try {
				Character feedback = InfoScreen.feedback();
				if (!forceselection && feedback == 'q') {
					return -1;
				}
				int selected = Integer.parseInt(feedback.toString()) - 1;
				if (selected < names.size()) {
					return selected;
				}
			} catch (Exception e) {
				continue;
			}
		}
	}
}
