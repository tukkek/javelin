package javelin.controller.exception.battle;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.JavelinApp;
import javelin.controller.BattleSetup;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.fight.Fight;
import javelin.controller.upgrade.Spell;
import javelin.model.item.Item;
import javelin.model.item.Wand;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.screen.BattleScreen;
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
		ArrayList<Combatant> foes = fight.setup();
		if (fight.avoid(foes)) {
			return;
		}
		StartBattle.preparebattle(foes);
		BattleSetup.place();
		Fight.state.checkwhoisnext();
		fight.ready();
		BattlePanel.current = Fight.state.next;
		BattleScreen battleScreen = new BattleScreen(true);
		battleScreen.mainLoop();
	}

	/**
	 * Runs a strategic combat instead of opening a {@link BattleScreen}.
	 */
	public static void quickbattle() {
		ArrayList<Combatant> opponents = Javelin.app.fight.setup();
		if (Javelin.app.fight.avoid(opponents)) {
			return;
		}
		float resourcesused = ChallengeRatingCalculator.useresources(
				ChallengeRatingCalculator.calculateel(Squad.active.members),
				ChallengeRatingCalculator.calculateel(opponents));
		ArrayList<Combatant> original =
				new ArrayList<Combatant>(Squad.active.members);
		for (Combatant c : original) {
			strategicdamage(c, resourcesused);
		}
		if (Squad.active.members.isEmpty()) {
			Javelin.message("Battle report: Squad lost in combat!", false);
			Squad.active.disband();
			return;
		}
		Fight.victory = true;
		preparebattle(opponents);
		EndBattle.showcombatresult(WorldScreen.active, original,
				"Battle report: ");
	}

	static void strategicdamage(Combatant c, float resourcesused) {
		c.hp -= c.maxhp * resourcesused;
		if (c.hp <= Combatant.DEADATHP || //
				(c.hp <= 0 && RPG.random() < Math
						.abs(c.hp / new Float(Combatant.DEADATHP)))) {
			Squad.active.members.remove(c);
			return;
		}
		if (c.hp <= 0) {
			c.hp = 1;
		}
		for (Spell s : c.spells) {
			for (int i = s.used; i < s.perday; i++) {
				if (RPG.random() < resourcesused) {
					s.used += 1;
				}
			}
		}
		ArrayList<Item> bag = Squad.active.equipment.get(c.id);
		for (Item i : new ArrayList<Item>(bag)) {
			if (i.usedinbattle) {
				if (i instanceof Wand) {
					Wand w = (Wand) i;
					w.charges -= w.charges * resourcesused;
					if (w.charges <= 0) {
						bag.remove(w);
					}
				} else if (RPG.random() < resourcesused) {
					bag.remove(i);
				}
			}
		}
	}

	/** TODO deduplicate originals */
	static public void preparebattle(ArrayList<Combatant> opponents) {
		JavelinApp.lastenemies.clear();
		Fight.state.redTeam = opponents;
		for (final Combatant m : Fight.state.redTeam) {
			JavelinApp.lastenemies.add(m.source.clone());
		}
		JavelinApp.originalteam = cloneteam(Fight.state.blueTeam);
		JavelinApp.originalfoes = cloneteam(Fight.state.redTeam);
		BattleScreen.originalblueteam =
				new ArrayList<Combatant>(Fight.state.blueTeam);
		BattleScreen.originalredteam =
				new ArrayList<Combatant>(Fight.state.redTeam);
	}

	static ArrayList<Combatant> cloneteam(ArrayList<Combatant> team) {
		ArrayList<Combatant> clone = new ArrayList<Combatant>(team.size());
		for (Combatant c : team) {
			clone.add(c.clone());
		}
		return clone;
	}
}
