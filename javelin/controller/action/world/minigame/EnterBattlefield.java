package javelin.controller.action.world.minigame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.Battle;
import javelin.controller.fight.minigame.BattlefieldFight;
import javelin.controller.fight.minigame.BattlefieldFight.Reinforcement;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.Battlefield;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * @see Battlefield
 * @see Battle
 * @author alex
 */
public class EnterBattlefield extends EnterMinigame {
	BattleSize[] SIZES = new BattleSize[] { new BattleSize("Brawl", 1, 4),
			new BattleSize("Skirmish", 2, 4), new BattleSize("Standard", 3, 4),
			new BattleSize("War", 4, 4), new BattleSize("Apocalypse", 5, 4),
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

	public EnterBattlefield() {
		super("Battlefield (mini-game)", new int[] {}, new String[] { "B" });
	}

	@Override
	public void perform(WorldScreen screen) {
		super.perform(screen);
		BattlefieldFight f = new BattlefieldFight();
		if (setup(f)) {
			throw new StartBattle(f);
		}
	}

	boolean setup(BattlefieldFight f) {
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
					+ "\nPress any key to continue...");
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
		ArrayList<Combatant> squad = f.selectredsquad(el);
		f.redsquads.add(squad);
		f.redarmy.addAll(squad);
		return "Your opponent's choice: " + Combatant.group(squad) + ".";
	}

	boolean pickblue(String prompt, int el, BattlefieldFight f) {
		ArrayList<Combatant> squad = BattlefieldFight.recruitbluesquad(
				prompt + "Choose your next sqaud (" + el + " army points):",
				new Reinforcement(el), false);
		if (squad == null) {
			return false;
		}
		f.bluequads.add(squad);
		f.bluearmy.addAll(squad);
		return true;
	}
}
