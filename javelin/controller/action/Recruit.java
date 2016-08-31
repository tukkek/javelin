package javelin.controller.action;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.fight.Fight;
import javelin.controller.fight.Rush;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Spawner;
import javelin.model.world.location.unique.minigame.DungeonRush;
import javelin.view.screen.BattleScreen;

/**
 * Allows to spawn allies in {@link DungeonRush}.
 * 
 * @author alex
 */
public class Recruit extends Action {
	/** Constructor. */
	public Recruit() {
		super("Recruit (dungeon rush)", "R");
	}

	@Override
	public boolean perform(Combatant active) {
		if (!(Javelin.app.fight instanceof Rush)) {
			Game.message(
					"This action can only be used in the Dungeon Rush mini-game...",
					Delay.WAIT);
			return false;
		}
		DungeonRush dr = DungeonRush.get();
		Rush rush = (Rush) Javelin.app.fight;
		ArrayList<Monster> spawners = new ArrayList<Monster>(dr.spawners);
		for (Monster m : new ArrayList<Monster>(spawners)) {
			if (hasspawner(m) == null || getcost(m) > rush.mana) {
				spawners.remove(m);
			}
		}
		ArrayList<String> choices = new ArrayList<String>(spawners.size());
		for (Monster spawner : spawners) {
			int cost = getcost(spawner);
			choices.add(spawner + " (" + cost + " mana)");
		}
		int choice = Javelin.choose(
				"You have " + rush.mana + " mana.\n\nRecruit which unit?",
				choices, true, false);
		Javelin.app.switchScreen(BattleScreen.active);
		if (choice == -1) {
			return false;
		}
		Combatant unit = hasspawner(spawners.get(choice));
		Spawner s = (Spawner) unit.source;
		rush.mana -= getcost(s);
		Fight.state.blueTeam.add(s.spawn(unit, active.ap));
		Game.redraw();
		return true;
	}

	int getcost(Monster spawner) {
		return Math.round(spawner.challengeRating * DungeonRush.PLAYERMANAMULTIPLIER);
	}

	Combatant hasspawner(Monster summon) {
		for (Combatant c : Fight.state.blueTeam) {
			Spawner s = c.source instanceof Spawner ? (Spawner) c.source : null;
			if (s != null && s.summon.equals(summon)) {
				return c;
			}
		}
		return null;
	}
}
