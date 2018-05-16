package javelin.controller.action.world.minigame;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.InfiniteList;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.scenario.Campaign;
import javelin.model.item.Tier;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.screen.SquadScreen;
import javelin.view.screen.WorldScreen;

/**
 * Allows access to the Arena at any point in time (unless in battle).
 *
 * @author alex
 */
public class EnterArena extends EnterMinigame {
	/** Constructor. */
	public EnterArena() {
		super("Arena (mini-game)", new int[] { KeyEvent.VK_A },
				new String[] { "A" });
	}

	@Override
	public void perform(WorldScreen screen) {
		super.perform(screen);
		Combatants original = Arena.get().gladiators;
		Combatants gladiators = new Combatants(original);
		if (Javelin.prompt("Start an arena match?\n" + listcurrent(gladiators)
				+ "\nPress ENTER start or any other key to cancel...") != '\n') {
			return;
		}
		boolean start;
		if (gladiators.isEmpty()) {
			double crmin = Integer.MIN_VALUE;
			double crmax = SquadScreen.SELECTABLE[SquadScreen.SELECTABLE.length
					- 1];
			float targetel = Campaign.INITIALEL;
			start = choosegladiators(crmin, crmax, targetel, gladiators);
		} else {
			start = resume(gladiators, original);
		}
		if (start) {
			original.clear();
			throw new StartBattle(new ArenaFight(gladiators));
		}
	}

	boolean resume(Combatants gladiators, Combatants original) {
		float levels = 0;
		for (Combatant c : gladiators) {
			levels += c.source.cr;
		}
		int averagepartylevel = Math.round(levels / gladiators.size());
		int tier = Tier.get(averagepartylevel).ordinal();
		if (tier >= 4) {
			String prompt = "Your current gladiators are too strong for the arena!\n"
					+ "Shall they be liberated?\n"
					+ "Press l to liberate them or any other key to cancel...";
			if (Javelin.prompt(prompt) == 'l') {
				original.clear();
			}
			return false;
		}
		double crmin = tier * 5;
		double crmax = (crmin + 1) * 5;
		float targetel = averagepartylevel + 4;
		return choosegladiators(crmin, crmax, targetel, gladiators);
	}

	String listcurrent(Combatants gladiators) {
		if (gladiators.isEmpty()) {
			return "";
		}
		String current = "Your current gladiators: ";
		for (Combatant c : gladiators) {
			current += c + " (level " + Math.round(c.source.cr) + "), ";
		}
		return current.substring(0, current.length() - 2) + ".";
	}

	boolean choosegladiators(double crmin, double crmax, float targetel,
			Combatants gladiators) {
		InfiniteList<Monster> candidates = getcandidates(crmin, crmax);
		while (ChallengeCalculator.calculateel(gladiators) < targetel) {
			ArrayList<Monster> page = candidates.pop(3);
			ArrayList<String> names = new ArrayList<String>(3);
			for (int i = 0; i < 3; i++) {
				Monster m = page.get(i);
				names.add(m + " (level " + Math.round(m.cr) + ")");
			}
			String prompt = "Select your gladiators:";
			int choice = Javelin.choose(prompt, names, false, false);
			if (choice == -1) {
				return false;
			}
			Monster m = page.get(choice);
			Combatant c = new Combatant(m, true);
			c.maxhp = m.hd.maximize();
			c.hp = c.maxhp;
			gladiators.add(c);
			candidates.remove(m);
		}
		return true;
	}

	InfiniteList<Monster> getcandidates(double crmin, double crmax) {
		InfiniteList<Monster> candidates = new InfiniteList<Monster>();
		for (float cr : Javelin.MONSTERSBYCR.keySet()) {
			if (crmin <= cr && cr <= crmax) {
				for (Monster m : Javelin.MONSTERSBYCR.get(cr)) {
					if (!m.internal) {
						candidates.add(m);
					}
				}
			}
		}
		return candidates;
	}
}
