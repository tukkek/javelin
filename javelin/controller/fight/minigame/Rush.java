package javelin.controller.fight.minigame;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.Weather;
import javelin.controller.action.Recruit;
import javelin.controller.db.StateManager;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.map.Map;
import javelin.controller.map.terrain.underground.Floor;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Monster;
import javelin.model.unit.Spawner;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.unique.minigame.DungeonRush;
import tyrant.mikera.engine.RPG;

/**
 * @see DungeonRush
 * @author alex
 */
public class Rush extends Minigame {
	class RushSetup extends BattleSetup {
		private static final int CLEARINGAREA = 9;

		@Override
		public void place() {
			Point reda = new Point(1, 1);
			Point redb = new Point(state.map.length - 2, CLEARINGAREA);
			clear(reda, redb);
			place(reda, redb, state.redTeam);
			Point bluea = new Point(1, state.map[0].length - 2 - CLEARINGAREA);
			Point blueb = new Point(state.map.length - 2,
					state.map[0].length - 2);
			clear(bluea, blueb);
			place(bluea, blueb, state.blueTeam);
			Combatant spawner = RPG.pick(state.blueTeam);
			Combatant leader = ((Spawner) spawner.source).spawn(spawner, 0f);
			state.blueTeam.add(leader);
			mana -= leader.source.challengerating
					* DungeonRush.PLAYERMANAMULTIPLIER;
		}

		void place(Point reda, Point redb, ArrayList<Combatant> redTeam) {
			for (Combatant c : redTeam) {
				add(c, getrandompoint(state, reda.x, redb.x, reda.y, redb.y));
			}
		}

		void clear(Point reda, Point redb) {
			for (int x = reda.x; x <= redb.x; x++) {
				for (int y = reda.y; y <= redb.y; y++) {
					state.map[x][y].blocked = false;
				}
			}
		}

		@Override
		public void rollinitiative() {
			// don't
		}
	}

	static final Map DUNGEON = new Floor();

	/**
	 * Resources used by the human player to {@link Recruit} units.
	 * 
	 * @see DungeonRush#PLAYERMANAMULTIPLIER
	 */
	public int mana;

	Float lastupdate = null;

	/** Constructor.. */
	public Rush(DungeonRush r) {
		for (Monster spawner : r.spawners) {
			mana += spawner.challengerating * DungeonRush.PLAYERMANAMULTIPLIER;
		}
		setup = new RushSetup();
		meld = true;
		period = RPG
				.pick(new String[] { Javelin.PERIODMORNING, Javelin.PERIODNOON,
						Javelin.PERIODEVENING, Javelin.PERIODNIGHT });
		weather = Math.min(RPG.pick(Weather.DISTRIBUTION), map.maxflooding);
		map.flooded = DUNGEON.flooded;
		map.floor = DUNGEON.floor;
		map.obstacle = DUNGEON.obstacle;
		map.rock = DUNGEON.rock;
		map.wall = DUNGEON.wall;
		map.wallfloor = DUNGEON.wallfloor;
	}

	@Override
	public ArrayList<Combatant> getmonsters(Integer teamel) {
		DungeonRush dr = DungeonRush.get();
		ArrayList<Combatant> monsters = new ArrayList<Combatant>(
				dr.spawners.size());
		for (Monster s : dr.spawners) {
			s = RPG.pick(Javelin.MONSTERSBYCR.get(s.challengerating));
			monsters.add(new Spawner(s, false).getcombatant());
		}
		return monsters;
	}

	@Override
	public ArrayList<Combatant> getblueteam() {
		DungeonRush dr = DungeonRush.get();
		ArrayList<Combatant> monsters = new ArrayList<Combatant>(
				dr.spawners.size());
		for (Monster s : dr.spawners) {
			monsters.add(new Spawner(s, true).getcombatant());
		}
		return monsters;
	}

	@Override
	public boolean onend() {
		DungeonRush dr = DungeonRush.get();
		if (Fight.victory) {
			dr.upgrade(true);
		} else {
			Monster removed = dr.downgrade();
			String message = "You have lost...";
			if (removed != null) {
				message += "\nYour " + removed + " spawner has been destroyed.";
			}
			Javelin.message(message, true);
		}
		StateManager.save(true, StateManager.SAVEFILE);
		return false;
	}

	@Override
	public void checkendbattle() {
		if (Fight.state.redTeam.isEmpty()) {
			throw new EndBattle();
		}
		boolean lost = true;
		for (Combatant c : Fight.state.blueTeam) {
			if (c.source instanceof Spawner) {
				continue;
			}
			lost = false;
			break;
		}
		if (lost) {
			Fight.state.blueTeam.clear();
			throw new EndBattle();
		}
	}

	@Override
	public void meld(Combatant hero, Meld m) {
		super.meld(hero, m);
		Game.messagepanel.clear();
		int manabonus = Math.round(m.cr * DungeonRush.PLAYERMANAMULTIPLIER);
		Game.message("You capture " + manabonus + " mana!", Delay.BLOCK);
		mana += manabonus;
	}

	@Override
	public void endturn() {
		super.endturn();
		state.next();
		if (lastupdate == null) {
			lastupdate = state.next.ap;
			return;
		}
		float ellapsed = state.next.ap - lastupdate;
		if (ellapsed > 0) {
			lastupdate = state.next.ap;
			for (Combatant c : new ArrayList<Combatant>(state.redTeam)) {
				if (c.source instanceof Spawner) {
					Spawner s = (Spawner) c.source;
					s.mana += ellapsed;
					if (s.mana >= s.challengerating) {
						s.mana -= s.challengerating;
						if (!DungeonRush.DEBUG) {
							state.redTeam.add(s.spawn(c, state.next.ap));
						}
					}
				}
			}
		}
	}

	@Override
	public Meld addmeld(int x, int y, Combatant dead, BattleState s) {
		Meld m = super.addmeld(x, y, dead, s);
		if (m != null) {
			m.meldsat = -Float.MAX_VALUE;
		}
		return m;
	}
}
