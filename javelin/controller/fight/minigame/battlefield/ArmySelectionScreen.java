package javelin.controller.fight.minigame.battlefield;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.old.RPG;

public class ArmySelectionScreen {
	BattleSize[] SIZES = new BattleSize[] { new BattleSize("Brawl", 1, 4),
			new BattleSize("Skirmish", 2, 4), new BattleSize("Battle", 3, 4),
			new BattleSize("War", 4, 4), new BattleSize("Armageddon", 5, 4),
			new BattleSize("Random", 1, 20), };

	class BattleSize {
		String description;
		int dies;
		int faces;

		public BattleSize(String description, int dies, int faces) {
			this.description = description;
			this.dies = dies;
			this.faces = faces;
		}

		@Override
		public String toString() {
			int from = dies;
			int to = dies * faces;
			return description + " (between " + from + " and " + to
					+ " squads)";
		}
	}

	public boolean selectarmy(BattlefieldFight f) {
		BattleSize size = choosesize();
		if (size == null) {
			return false;
		}
		List<Integer> els = rollels(size);
		if (els == null) {
			return false;
		}
		for (int el : els) {
			boolean bluepickfirst = RPG.chancein(2);
			if (!pick(bluepickfirst, el, f) || !pick(!bluepickfirst, el, f)) {
				return false;
			}
		}
		return true;
	}

	boolean pick(boolean bluepick, int el, BattlefieldFight f) {
		if (!bluepick) {
			String pick = pickred(el, f);
			Javelin.promptscreen(describearmies(f) + pick
					+ "\n\nPress any key to continue...");
			return true;
		}
		return pickblue(describearmies(f), el, f);
	}

	List<Integer> rollels(BattleSize s) {
		ArrayList<Integer> els = null;
		while (els == null) {
			int squads = 0;
			for (int i = 0; i < s.dies; i++) {
				squads += RPG.r(1, s.faces);
			}
			els = new ArrayList<Integer>(squads);
			for (int i = 0; i < squads; i++) {
				els.add(RPG.r(1, BattlefieldFight.HIGHESTEL));
			}
			String prompt = "Proceed with these army points for each squad?\n"
					+ "Press ENTER to confirm, q to quit or any other key to reroll...\n\n";
			Collections.sort(els);
			Collections.reverse(els);
			char input = Javelin.promptscreen(prompt + els);
			if (input == 'q') {
				return null;
			}
			if (input != '\n') {
				els = null;
			}
		}
		return els;
	}

	BattleSize choosesize() {
		ArrayList<String> sizes = new ArrayList<String>();
		for (BattleSize s : SIZES) {
			sizes.add(s.toString());
		}
		int choice = Javelin.choose("Choose the size of this battle:", sizes,
				true, false);
		return choice >= 0 ? SIZES[choice] : null;
	}

	public String describearmies(BattlefieldFight f) {
		String prompt = describearmy("Your army", f.bluearmy);
		prompt += describearmy("Opponent's army", f.redarmy);
		return prompt;
	}

	public String describearmy(String header, ArrayList<Combatant> army) {
		header += ":\n";
		header += army.isEmpty() ? "Empty" : Combatant.group(army);
		return header + ".\n\n";
	}

	String pickred(int el, BattlefieldFight f) {
		ArrayList<Combatant> squad = f.recruitredsquad(el);
		f.redsquads.add(squad);
		f.redarmy.addAll(squad);
		return "Your opponent's choice: " + Combatant.group(squad) + ".";
	}

	boolean pickblue(String prompt, int el, BattlefieldFight f) {
		Reinforcement r = new Reinforcement(el);
		ArrayList<Combatant> squad = BattlefieldFight.recruitbluesquad(
				prompt + "Choose your next squad (" + el + " army points):", r,
				false);
		if (squad == null) {
			return false;
		}
		if (squad == r.footsoldiers) {
			for (Combatant c : squad) {
				c.setmercenary(true);
			}
		}
		f.bluequads.add(squad);
		f.bluearmy.addAll(squad);
		return true;
	}
}
