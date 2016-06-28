package javelin.view.screen;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.RandomEncounter;
import javelin.model.BattleMap;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.Trap;
import javelin.view.Images;
import tyrant.mikera.engine.Thing;
import tyrant.mikera.tyrant.Game;

/**
 * Shows the inside of a {@link Dungeon}.
 * 
 * @author alex
 */
public class DungeonScreen extends WorldScreen {
	private static final Image FLOOR = Images.getImage("dungeonfloor");
	private static final Image WALL = Images.getImage("dungeonwall");
	/** TODO hack */
	public static boolean dontmove;

	/** Exhibits a dungeon. */
	public DungeonScreen(BattleMap map) {
		super(map);
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
		Dungeon.active.herolocation = new Point(Game.hero().x, Game.hero().y);
	}

	@Override
	public void explore(float hoursellapsed) {
		try {
			RandomEncounter.encounter(Dungeon.ENCOUNTERRATIO);
		} catch (StartBattle e) {
			map.removeThing(Dungeon.active.hero);
			throw e;
		}
	}

	@Override
	public boolean react(WorldActor actor, int x, int y) {
		int searchroll = Squad.active.search();
		for (Feature f : new ArrayList<Feature>(Dungeon.active.features)) {
			if (f.x == x && f.y == y) {
				if (!f.activate()) {
					DungeonScreen.dontmove = true;
					return true;
				}
				f.remove();
			}
			if (x - 1 <= f.x && f.x <= x + 1 && y - 1 <= f.y && f.y <= y + 1) {
				Trap t = f instanceof Trap ? (Trap) f : null;
				if (t != null && !t.found) {
					if (searchroll >= t.searchdc) {
						t.found = true;
						t.generate(map);
					}
				}
			}
		}
		return false;
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
	public Thing gethero() {
		return Dungeon.active.hero;
	}

	@Override
	public boolean scale() {
		return false;
	}

	@Override
	public Image gettile(int x, int y) {
		return Dungeon.active.walls.contains(new Point(x, y)) ? WALL : FLOOR;
	}
}
