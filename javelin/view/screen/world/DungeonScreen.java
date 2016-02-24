package javelin.view.screen.world;

import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.BattleMap;
import javelin.model.dungeon.Feature;
import javelin.model.world.Dungeon;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Shows the inside of a {@link Dungeon}.
 * 
 * @author alex
 */
public class DungeonScreen extends WorldScreen {
	public DungeonScreen(BattleMap mapp) {
		super(mapp);
		mappanel.tilesize = 32;
		mappanel.discovered = new HashSet<Point>() {
			@Override
			public boolean add(Point e) {
				return false;
			};

			@Override
			public boolean contains(Object o) {
				return false;
			};
		};
	}

	@Override
	public void turn() {
		Dungeon.active.herop = new Point(Game.hero().x, Game.hero().y);
	}

	@Override
	public void encounter() {
		try {
			WorldScreen.encounter(Dungeon.ENCOUNTERRATIO);
		} catch (StartBattle e) {
			map.removeThing(Dungeon.active.hero);
			throw e;
		}
	}

	@Override
	public boolean react(Thing t, WorldMove worldMove) {
		for (Feature f : Dungeon.active.features) {
			if (f.x == t.x && f.y == t.y) {
				f.activate();
				if (Dungeon.active != null) {
					f.visual.remove();
					Dungeon.active.features.remove(f);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean entertown(Thing t, WorldScreen s, int x, int y) {
		return false;
	}

	@Override
	public void ellapse(int suggested) {
		// time stands still
	}

	public static void message(String in) {
		Game.messagepanel.clear();
		Game.message(in, null, Delay.BLOCK);
		Game.getInput();
	}

	@Override
	public boolean allowmove(int x, int y) {
		return !Dungeon.active.walls
				.contains(new javelin.controller.Point(x, y));
	}

	@Override
	public void updatelocation(int x, int y) {
		// don't
	}

	@Override
	public void view(Thing h) {
		for (int x = -1; x <= +1; x++) {
			for (int y = -1; y <= +1; y++) {
				try {
					Dungeon.active.visible[h.x + x][h.y + y] = true;
				} catch (ArrayIndexOutOfBoundsException e) {
					continue;
				}
			}
		}
		map.makeAllInvisible();
		for (int x = 0; x < Dungeon.SIZE; x++) {
			for (int y = 0; y < Dungeon.SIZE; y++) {
				if (Dungeon.active.visible[x][y]) {
					map.setVisible(x, y);
				}
			}
		}
	}

	@Override
	public Thing updatehero() {
		return Dungeon.active.hero;
	}

	@Override
	public boolean scale() {
		return false;
	}
}
