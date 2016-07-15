package javelin.view.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.view.screen.upgrading.SkillSelectionScreen;
import tyrant.mikera.engine.RPG;

/**
 * Squad selection screen when starting a new game.
 * 
 * @author alex
 */
public class SquadScreen extends InfoScreen {
	private static final float INITIALELTARGET = 5f;
	static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	private static final int MONSTERPERPAGE = ALPHABET.indexOf('y');
	static final float[] SELECTABLE = { 1f, 1.25f };
	private final ArrayList<Monster> candidates = getcandidates();
	ArrayList<Combatant> squad = new ArrayList<Combatant>();

	SquadScreen() {
		super("");
		Collections.sort(candidates, new Comparator<Monster>() {
			@Override
			public int compare(Monster o1, Monster o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
	}

	ArrayList<Combatant> select() {
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
			fillwithrandom: while (!checkifsquadfull()) {
				Monster candidate = RPG.pick(candidates);
				for (Combatant m : squad) {
					if (m.source.name.equals(candidate.name)) {
						continue fillwithrandom;
					}
				}
				recruit(candidate);
			}
		} else if (input == '\n') {
			if (squad.isEmpty()) {
				page(index);
			}
			upgrade();
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

	void upgrade() {
		float startingcr = totalcr();
		while (ChallengeRatingCalculator.calculateel(squad) < INITIALELTARGET) {
			Combatant weakest = squad.get(0);
			for (int i = 1; i < squad.size(); i++) {
				Combatant c = squad.get(i);
				if (c.source.challengeRating < weakest.source.challengeRating) {
					weakest = c;
				}
			}
			Commoner.SINGLETON.apply(weakest);
			ChallengeRatingCalculator.calculateCr(weakest.source);
		}
		for (Combatant c : squad) {
			if (SkillSelectionScreen.canspend(c.source)) {
				c.source.purchaseskills(Commoner.SINGLETON).show();
			}
		}
		Squad.active.gold = RewardCalculator.getgold(totalcr() - startingcr);
	}

	private float totalcr() {
		int cr = 0;
		for (Combatant c : squad) {
			cr += ChallengeRatingCalculator.calculateCr(c.source);
		}
		return cr;
	}

	private void recruit(Monster m) {
		Combatant c = Javelin.recruit(m);
		c.hp = c.source.hd.maximize();
		c.maxhp = c.hp;
		squad.add(c);
	}

	boolean checkifsquadfull() {
		return ChallengeRatingCalculator.calculateel(squad) >= INITIALELTARGET;
	}

	int printpage(int index, int next) {
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
		new SquadScreen().select();
	}

	/**
	 * @return All possible monster types that can be used as starting party
	 *         members.
	 */
	public static ArrayList<Monster> getcandidates() {
		ArrayList<Monster> candidates = new ArrayList<Monster>();
		for (float cr : SELECTABLE) {
			List<Monster> tier = Javelin.MONSTERSBYCR.get(cr);
			if (tier != null) {
				candidates.addAll(tier);
			}
		}
		return candidates;
	}
}
