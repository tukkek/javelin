package javelin.controller.action.world.minigame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.sun.glass.events.KeyEvent;

import javelin.Javelin;
import javelin.controller.action.world.WorldAction;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.Battle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.location.unique.minigame.Battlefield;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * @see Battlefield
 * @see Battle
 * @author alex
 */
public class EnterBattlefield extends WorldAction {
	static final String KEYS = "abcdefgh";
	static final int TARGETEL = 18;
	static final int NELITE = 3;
	static final int NFOOTMEN = 5;

	ArrayList<Combatant> blueteam = new ArrayList<Combatant>();
	ArrayList<Combatant> redteam = new ArrayList<Combatant>();
	ArrayList<Monster> recruits = new ArrayList<Monster>(NELITE + NFOOTMEN);

	public EnterBattlefield() {
		super("Battlefield (mini-game)", new int[] { KeyEvent.VK_B },
				new String[] { "b" });
	}

	@Override
	public void perform(WorldScreen screen) {
		blueteam.clear();
		redteam.clear();
		recruits.clear();
		// if (Javelin.prompt("Start a battlefield match?\n\n"//
		// + "Press ENTER to confirm or any other key to cancel...") != '\n') {
		// return;
		// }
		if (!pickcommanders()) {
			return;
		}
		pool(getpool(6, 10), recruits, NELITE);
		pool(getpool(1, 5), recruits, NFOOTMEN + NELITE);
		while (ChallengeRatingCalculator.calculateel(blueteam) < TARGETEL) {
			if (!manage()) {
				return;
			}
		}
		int blueel = ChallengeRatingCalculator.calculateel(blueteam);
		int redel = ChallengeRatingCalculator.calculateel(redteam);
		if (blueel != redel) {
			complete(blueel > redel ? redteam : blueteam,
					Math.max(blueel, redel));
		}
		throw new StartBattle(new Battle(blueteam, redteam));
	}

	boolean pickcommanders() {
		ArrayList<Monster> commanders = getpool(11, 15);
		while (commanders.size() > 7) {
			commanders.remove(RPG.r(0, commanders.size() - 1));
		}
		commanders.sort(new Comparator<Monster>() {
			@Override
			public int compare(Monster o1, Monster o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		ArrayList<String> commanderlist = new ArrayList<String>(7);
		for (Monster m : commanders) {
			commanderlist.add(m + level(m));
		}
		int choice = Javelin.choose(
				"Welcome to the battlefield mini-game!\n\nSelect your commander:",
				commanderlist, true, false);
		if (choice < 0) {
			return false;
		}
		Monster commander = commanders.get(choice);
		blueteam.add(new Combatant(commander.clone(), true));
		commanders.remove(commander);
		redteam.add(new Combatant(RPG.pick(commanders).clone(), true));
		return true;
	}

	void complete(ArrayList<Combatant> team, int el) {
		Monster weakest = null;
		for (Combatant c : team) {
			if (weakest == null
					|| c.source.challengeRating < weakest.challengeRating) {
				weakest = c.source;
			}
		}
		while (ChallengeRatingCalculator.calculateel(team) < el) {
			team.add(new Combatant(weakest.clone(), true));
		}
	}

	void pool(ArrayList<Monster> from, ArrayList<Monster> to, int limit) {
		while (to.size() < limit) {
			Monster pick = RPG.pick(from);
			if (!to.contains(pick)) {
				to.add(pick);
			}
		}
	}

	boolean manage() {
		String text = list(blueteam, true, "Your army");
		text += "--------------------------------------------------------\n";
		text += list(redteam, false, "Opposing army");
		text += "Recruit from which group? (press q to quit)";
		InfoScreen screen = new InfoScreen("");
		screen.print(text);
		Character input = screen.getInput();
		if (input == 'q') {
			return false;
		}
		int recruit = KEYS.indexOf(input.charValue());
		if (recruit < 0) {
			return true;
		}
		boolean elite = recruit < NELITE;
		recruit(recruits.get(recruit), blueteam, elite);
		while (ChallengeRatingCalculator.calculateel(
				redteam) < ChallengeRatingCalculator.calculateel(blueteam)) {
			recruit(RPG.pick(elite ? getelite() : getfootmen()), redteam,
					elite);
		}
		return true;
	}

	private void recruit(Monster monster, ArrayList<Combatant> team,
			boolean elite) {
		int n = RPG.r(1, elite ? 4 : 8);
		for (int i = 0; i < n; i++) {
			if (count(team, monster) >= 50) {
				return;
			}
			team.add(new Combatant(monster.clone(), true));
		}
	}

	String list(ArrayList<Combatant> team, boolean shownumbers, String header) {
		String list = header + " (encounter level "
				+ ChallengeRatingCalculator.calculateel(team) + " of "
				+ TARGETEL + "):\n\n";
		Combatant commander = team.get(0);
		list += "Commander: " + commander + level(commander.source) + "\n\n";
		list += "Elite soldiers:\n\n";
		list += listtier(getelite(), team, shownumbers);
		list += "Footmen:\n\n";
		list += listtier(getfootmen(), team, shownumbers);
		return list;
	}

	List<Monster> getfootmen() {
		return recruits.subList(NELITE, NELITE + NFOOTMEN);
	}

	List<Monster> getelite() {
		return recruits.subList(0, NELITE);
	}

	String listtier(List<Monster> elite2, ArrayList<Combatant> team,
			boolean shownumbers) {
		String tier = "";
		for (int i = 0; i < elite2.size(); i++) {
			Monster m = elite2.get(i);
			tier += "    ";
			if (shownumbers) {
				tier += KEYS.charAt(recruits.indexOf(m)) + " - ";
			}
			tier += count(team, m) + " " + m.toString().toLowerCase() + level(m)
					+ "\n";
		}
		return tier + "\n";
	}

	int count(ArrayList<Combatant> team, Monster m) {
		int n = 0;
		for (Combatant c : team) {
			if (c.source.equals(m)) {
				n += 1;
			}
		}
		return n;
	}

	String level(Monster m) {
		return " (level " + Math.round(m.challengeRating) + ")";
	}

	ArrayList<Monster> getpool(int min, int max) {
		ArrayList<Monster> pool = new ArrayList<Monster>();
		for (float cr : Javelin.MONSTERSBYCR.keySet()) {
			if (min <= cr && cr <= max) {
				pool.addAll(Javelin.MONSTERSBYCR.get(cr));
			}
		}
		return pool;
	}
}
