package javelin.view.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.comparator.MonstersByName;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.World;
import tyrant.mikera.engine.RPG;

/**
 * Squad selection screen when starting a new game.
 * 
 * @author alex
 */
public class SquadScreen extends InfoScreen {
	static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
	static final int MONSTERPERPAGE = ALPHABET.indexOf('y');
	static final float[] SELECTABLE = { 1f, 1.25f, 1.5f };

	public static final ArrayList<Monster> CANDIDATES = new ArrayList<Monster>();

	static {
		for (float cr : SELECTABLE) {
			List<Monster> tier = Javelin.MONSTERSBYCR.get(cr);
			if (tier != null) {
				for (Monster candidate : tier) {
					if (candidate.isalive()) {
						CANDIDATES.add(candidate);
					}
				}
			}
		}
		Collections.sort(CANDIDATES, MonstersByName.INSTANCE);
	}

	ArrayList<Combatant> squad = new ArrayList<Combatant>();

	SquadScreen() {
		super("");
	}

	ArrayList<Combatant> select() {
		page(0);
		World.scenario.upgradesquad(squad);
		Squad.active.gold = getstartinggold();
		return squad;
	}

	public int getstartinggold() {
		int gold = 0;
		for (Combatant c : squad) {
			float level = c.source.cr - 1;
			if (level >= 1) {
				gold += RewardCalculator
						.calculatepcequipment(Math.round(level));
			}
		}
		return gold;
	}

	private void page(int index) {
		text = "Available monsters:\n";
		int next = index + MONSTERPERPAGE;
		int letter = printpage(index, next);
		Javelin.app.switchScreen(this);
		Character input = InfoScreen.feedback();
		if (input.equals(' ')) {
			page(next < CANDIDATES.size() ? next : 0);
		} else if (input == 'z') {
			fillwithrandom: while (!checkifsquadfull()) {
				Monster candidate = RPG.pick(CANDIDATES);
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
		} else {
			int selection = ALPHABET.indexOf(input);
			if (selection >= 0 && selection < letter) {
				recruit(CANDIDATES.get(index + selection));
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
		if (Javelin.DEBUG) {
			adddebugdata(c);
		}
	}

	boolean checkifsquadfull() {
		return World.scenario.checkfullsquad(squad);
	}

	int printpage(int index, int next) {
		int letter = 0;
		for (int i = index; i < next && i < CANDIDATES.size(); i++) {
			text += "\n" + ALPHABET.charAt(letter) + " - "
					+ CANDIDATES.get(i).toString();
			letter += 1;
		}
		text += "\n";
		text += "\nPress letter to select character";
		if (CANDIDATES.size() > MONSTERPERPAGE) {
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

	boolean first = true;

	void adddebugdata(Combatant c) {
	}
}
