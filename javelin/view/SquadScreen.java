package javelin.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Monster;
import javelin.view.screen.IntroScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.tyrant.InfoScreen;

public class SquadScreen extends InfoScreen {
	private static final int MONSTERPERPAGE = 20;
	static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	private final ArrayList<Monster> candidates;
	ArrayList<Monster> squad = new ArrayList<Monster>();

	public SquadScreen(ArrayList<Monster> candidates) {
		super("");
		this.candidates = candidates;
		Collections.sort(candidates, new Comparator<Monster>() {
			@Override
			public int compare(Monster o1, Monster o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
	}

	public ArrayList<Monster> select() {
		page(0);
		return squad;
	}

	private void page(int index) {
		text = "Available monsters:\n";
		int next = index + MONSTERPERPAGE;
		int letter = printpage(index, next);
		Javelin.app.switchScreen(this);
		Character input = IntroScreen.feedback();
		if (input.equals('\n')) {
			page(next < candidates.size() ? next : 0);
		} else if (input == 'z') {
			while (!checkifsquadfull()) {
				squad.add(RPG.pick(candidates));
			}
		} else {
			int selection = ALPHABET.indexOf(input);
			if (selection >= 0 && selection < letter) {
				squad.add(candidates.get(index + selection));
				if (checkifsquadfull()) {
					return;
				}
			}
			page(index);
		}
	}

	public boolean checkifsquadfull() {
		return ChallengeRatingCalculator.calculateSafe(squad) >= 5;
	}

	public int printpage(int index, int next) {
		int letter = 0;
		for (int i = index; i < next && i < candidates.size(); i++) {
			text += "\n" + ALPHABET.charAt(letter) + " - "
					+ candidates.get(i).toString();
			letter += 1;
		}
		text += "\n";
		text += "\nPress letter to select character";
		if (candidates.size() > MONSTERPERPAGE) {
			text += "\nPress ENTER to switch pages";
		}
		text += "\nPress z for a random team";
		text += "\n";
		text += "\nYour team:";
		text += "\n";
		for (Monster m : squad) {
			text += "\n" + m.toString();
		}
		return letter;
	}
}
