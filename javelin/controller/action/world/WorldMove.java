package javelin.controller.action.world;

import java.util.ArrayList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.exception.StartBattle;
import javelin.controller.fight.DungeonFight;
import javelin.controller.fight.RandomEncounter;
import javelin.model.BattleMap;
import javelin.model.unit.Combatant;
import javelin.model.world.Dungeon;
import javelin.model.world.Incursion;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.world.WorldScreen;
import tyrant.mikera.engine.RPG;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;

public class WorldMove extends WorldAction {
	public static final int TIMECOST = 4;
	public static boolean isleavingdungeon = false;
	private final int deltax;
	private final int deltay;

	public WorldMove(final int[] is, final int deltax, final int deltay,
			final String[] s) {
		super("Move, enter towns and dungeons, fight incursions", is, s);
		this.deltax = deltax;
		this.deltay = deltay;
	}

	@Override
	public void perform(final WorldScreen s) {
		Squad.active.hourselapsed += TIMECOST;
		final Thing t = Game.hero();
		if (entertown(t, s)) {
			return;
		}
		place(t, deltax, deltay);
		if (startdungeon(Dungeon.isdungeon(t.x, t.y))) {
			throw new StartBattle(new DungeonFight());
		}
		if (isleavingdungeon) {
			isleavingdungeon = false;
		} else {
			for (final Incursion spot : Incursion.squads) {
				if (spot.x == t.x && spot.y == t.y) {
					final Incursion i = spot;
					throw new StartBattle(i.getfight());
				}
			}
			if (walk(t)) {
				throw new StartBattle(new RandomEncounter());
			}
		}
		heal();
	}

	public static void place(final Thing t, final int deltax, final int deltay) {
		WorldScreen.worldmap.removeThing(t);
		t.x += deltax;
		t.y += deltay;
		if (t.x < 0) {
			t.x = 0;
		} else if (t.x > WorldScreen.worldmap.width - 1) {
			t.x = WorldScreen.worldmap.width - 1;
		}
		if (t.y < 0) {
			t.y = 0;
		} else if (t.y >= WorldScreen.worldmap.height - 1) {
			t.y = WorldScreen.worldmap.height - 1;
		}
		WorldScreen.worldmap.addThing(t, t.x, t.y);
		Squad.active.x = t.x;
		Squad.active.y = t.y;
		Squad.active.declareleader();
	}

	public static void heal() {
		for (final Combatant m : BattleMap.blueTeam) {
			if (m.source.fasthealing != 0) {
				m.hp = m.maxhp;
			}
		}
	}

	public static boolean walk(final Thing t) {
		final List<Squad> here = new ArrayList<Squad>();
		for (final Squad s : Squad.squads) {
			if (s.x == t.x && s.y == t.y) {
				here.add(s);
			}
		}
		if (here.size() > 1) {
			here.get(0).join(here.get(1));
		} else if (RPG.random() < .4 && !Javelin.DEBUGDISABLECOMBAT) {
			return true;
		}
		return false;
	}

	public static boolean startdungeon(final Dungeon d) {
		boolean fight = false;
		if (d != null) {
			d.visual.remove();
			Dungeon.dungeons.remove(d);
			fight = true;
		}
		return fight;
	}

	private boolean entertown(final Thing t, final WorldScreen s) {
		for (final Town town : Town.towns) {
			if (town.x == t.x + deltax && town.y == t.y + deltay) {
				town.enter(Squad.active);
				Javelin.app.switchScreen(BattleScreen.active);
				return true;
			}
		}
		return false;
	}

}
