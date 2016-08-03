package javelin.controller.fight;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.db.StateManager;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.map.Map;
import javelin.model.item.Item;
import javelin.model.state.BattleState;
import javelin.model.state.BattleState.Vision;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.unique.Arena;
import javelin.view.frame.arena.ArenaSetup;
import javelin.view.screen.BattleScreen;
import tyrant.mikera.engine.RPG;

/**
 * Fight for the {@link Arena}, using it's gladiators instead of the active
 * {@link Squad}.
 * 
 * @author alex
 */
public class ArenaFight extends Fight {
	/**
	 * {@link Arena#gladiators} and temporary allies.
	 * 
	 * @see ArenaSetup
	 */
	public ArrayList<Combatant> blueteam = new ArrayList<Combatant>();
	/** Enemies. */
	public ArrayList<Combatant> redteam = new ArrayList<Combatant>();
	/** Number of {@link Meld} to place at the start of battle. */
	public int nmeld = 0;
	/** Double or nothing bet. */
	public int bet = 0;

	/** Constructor. */
	public ArenaFight() {
		meld = true;
		friendly = true;
		hide = false;
		bribe = false;
		friendlylevel = Combatant.STATUSINJURED;
		weather = null;
		period = null;
	}

	@Override
	public int getel(int teamel) {
		throw new RuntimeException("Don't generate this way #arenafight");
	}

	@Override
	public ArrayList<Combatant> getmonsters(int teamel) {
		return null;
	}

	/**
	 * @param gladiators
	 *            Add clones to {@link #getblueteam()}.
	 */
	public void addgladiators(ArrayList<Combatant> gladiators) {
		for (Combatant gladiator : gladiators) {
			blueteam.add(gladiator.clone().clonesource());
		}
	}

	/** Fills up {@link #redteam} and unbound fields. */
	public void generate() {
		if (map == null) {
			drawmap();
		}
		if (period == null) {
			drawperiod();
		}
		if (weather == null) {
			drawweather();
		}
		redteam = generate(ChallengeRatingCalculator.calculateel(blueteam));
	}

	/** Binds {@link Fight#weather}. */
	public void drawweather() {
		Integer original = weather;
		while (weather == original) {
			weather = RPG.r(Weather.DRY, Weather.STORM);
		}
	}

	/** Binds {@link Fight#period}. */
	public void drawperiod() {
		String original = period;
		while (period == original) {
			period = RPG.pick(new String[] { Javelin.PERIODNOON,
					Javelin.PERIODEVENING, Javelin.PERIODNIGHT });
		}
	}

	@Override
	public ArrayList<Terrain> getterrains() {
		ArrayList<Terrain> terrains =
				new ArrayList<Terrain>(Terrain.ALL.length);
		for (Terrain t : Terrain.ALL) {
			if (!t.equals(Terrain.WATER)) {
				terrains.add(t);
			}
		}
		terrains.add(Terrain.UNDERGROUND);
		return terrains;
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		return blueteam;
	}

	@Override
	public boolean onEnd(BattleScreen screen, ArrayList<Combatant> originalTeam,
			BattleState s) {
		Arena arena = Arena.get();
		for (Combatant dead : s.dead) {
			if (dead.getNumericStatus() == Combatant.STATUSDEAD) {
				arena.gladiators.remove(dead);
			}
		}
		String result = "";
		if (victory) {
			result += "You won " + (bet + 1) + " coins!\n";
			arena.coins += bet + 1;
		} else if (bet > 0) {
			result += "You lost " + bet + " coins...\n";
			arena.coins -= bet;
		}
		Game.messagepanel.clear();
		Game.message("Your bet was " + bet + " coins.\n" + result
				+ "Press any key to continue...", Delay.BLOCK);
		screen.getUserInput();
		StateManager.save(true, StateManager.SAVEFILE);
		return false;
	}

	@Override
	public ArrayList<Item> getbag(Combatant combatant) {
		return Arena.get().getitems(combatant);
	}

	@Override
	public void ready() {
		meldplacement: while (nmeld > 0) {
			Point p = new Point(RPG.r(0, map.map.length - 1),
					RPG.r(0, map.map[0].length - 1));
			if (map.map[p.x][p.y].blocked
					|| Fight.state.getCombatant(p.x, p.y) != null) {
				continue;
			}
			for (Combatant c : Fight.state.getCombatants()) {
				if (Fight.state.hasLineOfSight(c, p) == Vision.CLEAR) {
					Fight.state.meld.add(new Meld(p.x, p.y, -Float.MAX_VALUE));
					nmeld -= 1;
					continue meldplacement;
				}
			}
		}
	}

	/** Binds {@link Fight#map}. */
	public void drawmap() {
		ArrayList<Terrain> terrains =
				new ArrayList<Terrain>(Terrain.ALL.length);
		for (Terrain t : Terrain.ALL) {
			if (!Terrain.WATER.equals(t)) {
				terrains.add(t);
			}
		}
		terrains.add(Terrain.UNDERGROUND);
		Map map = null;
		while (map == null || (this.map != null && map.equals(this.map))) {
			map = RPG.pick(terrains).getmaps().pick();
		}
		this.map = map;
	}
}
