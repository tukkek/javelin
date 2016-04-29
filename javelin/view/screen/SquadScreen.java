package javelin.view.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.classes.ClassAdvancement;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.Squad;
import tyrant.mikera.engine.RPG;

/**
 * Squad selection screen when starting a new game.
 * 
 * @author alex
 */
public class SquadScreen extends InfoScreen {
	private static final float INITIALELTARGET = 5f;
	private static final int MONSTERPERPAGE = 20;
	static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	static final float[] SELECTABLE = { 1 };
	private final ArrayList<Monster> candidates;
	ArrayList<Combatant> squad = new ArrayList<Combatant>();

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

	public ArrayList<Combatant> select() {
		page(0);
		return squad;
	}

	private void page(int index) {
		text = "Available monsters:\n";
		int next = index + MONSTERPERPAGE;
		int letter = printpage(index, next);
		Javelin.app.switchScreen(this);
		Character input = InfoScreen.feedback();
		if (input.equals(' ')) {
			page(next < candidates.size() ? next : 0);
		} else if (input == 'z') {
			while (!checkifsquadfull()) {
				recruit(RPG.pick(candidates));
			}
		} else if (input == '\n') {
			if (squad.isEmpty()) {
				page(index);
			}
			while (ChallengeRatingCalculator
					.calculateElSafe(squad) < INITIALELTARGET) {
				Combatant weakest = squad.get(0);
				for (int i = 1; i < squad.size(); i++) {
					Combatant c = squad.get(i);
					if (c.source.challengeRating < weakest.source.challengeRating) {
						weakest = c;
					}
				}
				ClassAdvancement.COMMONER.apply(weakest);
				ChallengeRatingCalculator.calculateCr(weakest.source);

			}
		} else {
			int selection = ALPHABET.indexOf(input);
			if (selection >= 0 && selection < letter) {
				recruit(candidates.get(index + selection));
				if (checkifsquadfull()) {
					return;
				}
			}
			page(index);
		}
	}

	private void recruit(Monster m) {
		Combatant c = Javelin.recruit(m);
		c.hp = c.source.hd.maximize();
		c.maxhp = c.hp;
		squad.add(c);
	}

	public boolean checkifsquadfull() {
		if (Javelin.DEBUGSTARTINGCR != null) {
			return squad.size() == 4;
		}
		return ChallengeRatingCalculator
				.calculateElSafe(squad) >= INITIALELTARGET;
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
			text += "\nPress SPACE to switch pages";
		}
		text += "\nPress z for a random team";
		text += "\nPress ENTER to coninue with current selection";
		text += "\n";
		text += "\nYour team:";
		text += "\n";
		for (Combatant m : squad) {
			text += "\n" + m.source.toString();
		}
		return letter;
	}

	/** Start first squad in the morning */
	public static void open() {
		Squad.active = new Squad(0, 0, 8, null);
		new SquadScreen(getcandidates()).select();
	}

	public static ArrayList<Monster> getcandidates() {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		if (Javelin.DEBUGSTARTINGCR == null) {
			for (float cr : SELECTABLE) {
				candidates.addAll(Javelin.MONSTERSBYCR.get(cr));
			}
		} else {
			candidates
					.addAll(Javelin.MONSTERSBYCR.get(Javelin.DEBUGSTARTINGCR));
		}
		return candidates;
	}
}
