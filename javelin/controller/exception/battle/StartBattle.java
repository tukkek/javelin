package javelin.controller.exception.battle;

import java.util.ArrayList;

import javelin.Debug;
import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.fight.minigame.Minigame;
import javelin.controller.fight.setup.BattleSetup;
import javelin.model.item.Item;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;
import tyrant.mikera.engine.RPG;

/**
 * A {@link Fight} has started.
 * 
 * @see BattleSetup
 * @author alex
 */
public class StartBattle extends BattleEvent {
	/** Controller for the battle. */
	public final Fight fight;

	/** Constructor. */
	public StartBattle(final Fight d) {
		fight = d;
	}

	/** Prepares and switches to a {@link BattleScreen}. */
	public void battle() {
		ArrayList<Combatant> foes = fight.init();
		if (fight.avoid(foes)) {
			return;
		}
		preparebattle(foes);
		fight.setup.setup();
		Fight.state.next();
		fight.ready();
		final int elred = ChallengeCalculator.calculateel(Fight.state.redTeam);
		final int elblue = ChallengeCalculator.calculateel(Squad.active.members);
		int diffifculty = elred - elblue;
		if (fight instanceof Minigame
				|| !Squad.active.skipcombat(diffifculty)) {
			if (Javelin.DEBUG) {
				Debug.onbattlestart();
			}
			BattlePanel.current = Fight.state.next;
			BattleScreen battleScreen = new BattleScreen(true, true);
			fight.draw();
			battleScreen.mainLoop();
		} else {
			quickbattle(diffifculty);
		}
	}

	/**
	 * Runs a strategic combat instead of opening a {@link BattleScreen}. The
	 * problem with this is that, being more predictable, makes it easier for a
	 * human player to just safely farm gold and XP on easy regions without much
	 * chance of death (even at low HP) - so an extra level of fair difficulty
	 * randomization is added here, if only to prevent players from farming in
	 * strategic mode without ever resting.
	 * 
	 * @param difficulty
	 */
	public void quickbattle(int difficulty) {
		difficulty += RPG.randomize(2);
		float resourcesused = ChallengeCalculator.useresources(difficulty);
		String report = "Battle report:\n\n";
		ArrayList<Combatant> blueteam = new ArrayList<Combatant>(
				Squad.active.members);
		ArrayList<Float> damage = damage(blueteam, resourcesused);
		for (int i = 0; i < blueteam.size(); i++) {
			report += strategicdamage(blueteam.get(i), damage.get(i)) + "\n\n";
		}
		if (Squad.active.equipment.count() == 0) {
			report += Squad.active.wastegold(resourcesused);
		}
		InfoScreen s = new InfoScreen("");
		s.print(report + "Press ENTER or s to continue...");
		Character feedback = s.getInput();
		while (feedback != '\n' && feedback != 's') {
			continue;
		}
		WorldScreen.active.center();
		Squad.active.gold -= Squad.active.gold * (resourcesused / 10f);
		if (Squad.active.members.isEmpty()) {
			Javelin.message("Battle report: Squad lost in combat!", false);
			Squad.active.disband();
		} else {
			Fight.victory = true;
			fight.onend();
		}
		Javelin.app.fight = null;
	}

	private ArrayList<Float> damage(ArrayList<Combatant> blueteam,
			float resourcesused) {
		ArrayList<Float> damage = new ArrayList<Float>(blueteam.size());
		while (damage.size() < blueteam.size()) {
			damage.add(0f);
		}
		float total = resourcesused * blueteam.size();
		float dealt = 0;
		float step = resourcesused / 2f;
		while (dealt < total) {
			int i = RPG.r(0, blueteam.size() - 1);
			if (damage.get(i) <= 1) {
				damage.set(i, damage.get(i) + step);
				dealt += step;
			}
		}
		return damage;
	}

	/**
	 * TODO this needs to be enhanced because currently fighting with full
	 * health in a EL-1 battle will result in everyone surviving with 1% health
	 * or something, making this very easy to abuse. A better option might be to
	 * introduce some randomness on the difficulty used to calculate this or
	 * think of a new system where the damage can be distributed randomly
	 * between party members (instead of uniformly) or even "cancel" units of
	 * same CR before doing calculations.
	 * 
	 * @return
	 */
	static String strategicdamage(Combatant c, float resourcesused) {
		c.hp -= c.maxhp * resourcesused;
		boolean killed = c.hp <= Combatant.DEADATHP || //
				(c.hp <= 0 && RPG.random() < Math
						.abs(c.hp / new Float(Combatant.DEADATHP)));
		String report = "";
		ArrayList<Item> bag = Squad.active.equipment.get(c.id);
		for (Item i : new ArrayList<Item>(bag)) {
			String used = "";
			if (i.waste) {
				String wasted = i.waste(resourcesused, c, bag);
				if (wasted != null) {
					used += wasted + ", ";
				}
			}
			if (!used.isEmpty()) {
				report += " Used: " + used.substring(0, used.length() - 2)
						+ ".";
			}
		}
		if (killed) {
			Squad.active.remove(c);
			c.hp = -Integer.MAX_VALUE;
		} else {
			if (c.hp <= 0) {
				c.hp = 1;
			}
			report += c.wastespells(resourcesused);
		}
		return c + " is " + c.getstatus() + "." + report;
	}

	/** TODO deduplicate originals */
	static public void preparebattle(ArrayList<Combatant> opponents) {
		JavelinApp.lastenemies.clear();
		Fight.state.redTeam = opponents;
		for (final Combatant m : Fight.state.redTeam) {
			JavelinApp.lastenemies.add(m.source.clone());
		}
		Fight.originalblueteam = new ArrayList<Combatant>(Fight.state.blueTeam);
		Fight.originalredteam = new ArrayList<Combatant>(Fight.state.redTeam);
		for (int i = 0; i < Fight.state.blueTeam.size(); i++) {
			Combatant c = Fight.state.blueTeam.get(i);
			Fight.state.blueTeam.set(i, c.clone().clonesource());
		}
		Fight.state.next();
	}

	static ArrayList<Combatant> cloneteam(ArrayList<Combatant> team) {
		ArrayList<Combatant> clone = new ArrayList<Combatant>(team.size());
		for (Combatant c : team) {
			clone.add(c.clone());
		}
		return clone;
	}
}
