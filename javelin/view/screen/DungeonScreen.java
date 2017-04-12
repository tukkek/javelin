package javelin.view.screen;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomEncounter;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.Trap;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.dungeon.DungeonPanel;

/**
 * Shows the inside of a {@link Dungeon}.
 * 
 * @author alex
 */
public class DungeonScreen extends WorldScreen {
	/** TODO hack */
	public static boolean dontenter = false;
	/**
	 * <code>true</code> indicates something happened to stop further movement.
	 */
	public static boolean stopmovesequence = false;
	/**
	 * If <code>false</code> skip updating the location this time. TODO is hack?
	 */
	public static boolean updatelocation = true;
	public static final HashSet<Point> DISCOVEREDDUNGEON = new HashSet<Point>();

	@Override
	public boolean explore(float hoursellapsed, boolean encounter) {
		try {
			if (encounter) {
				RandomEncounter.encounter(Dungeon.ENCOUNTERRATIO);
			}
		} catch (StartBattle e) {
			throw e;
		}
		return !Dungeon.active.hazard();
	}

	@Override
	public boolean react(Actor actor, int x, int y) {
		int searchroll = Squad.active.search();
		for (Feature f : new ArrayList<Feature>(Dungeon.active.features)) {
			if (f.x == x && f.y == y) {
				boolean activated = f.activate();
				if (activated && f.remove) {
					f.remove();
					DungeonScreen.dontenter = !f.enter;
					DungeonScreen.stopmovesequence = f.stop;
				}
				return true;
			}
			if (x - 1 <= f.x && f.x <= x + 1 && y - 1 <= f.y && f.y <= y + 1) {
				Trap t = f instanceof Trap ? (Trap) f : null;
				if (t != null && !t.draw) {
					if (searchroll >= t.searchdc) {
						t.discover();
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
		Dungeon.active.herolocation = new Point(x, y);
	}

	@Override
	public void view(int xp, int yp) {
		for (int x = -1; x <= +1; x++) {
			for (int y = -1; y <= +1; y++) {
				try {
					Dungeon.active.setvisible(Dungeon.active.herolocation.x + x,
							Dungeon.active.herolocation.y + y);
				} catch (ArrayIndexOutOfBoundsException e) {
					continue;
				}
			}
		}
	}

	@Override
	public Image gettile(int x, int y) {
		return Images.getImage(Dungeon.active.walls.contains(new Point(x, y))
				? Dungeon.active.wall : Dungeon.active.floor);
	}

	@Override
	public Fight encounter() {
		return Dungeon.active.encounter();
	}

	@Override
	protected MapPanel getmappanel() {
		return new DungeonPanel();
	}

	@Override
	public boolean validatepoint(int tox, int toy) {
		return 0 <= tox && tox < Dungeon.SIZE && 0 <= toy && toy < Dungeon.SIZE;
	}

	@Override
	public Point getherolocation() {
		return Dungeon.active.herolocation;
	}

	@Override
	protected HashSet<Point> getdiscoveredtiles() {
		return DISCOVEREDDUNGEON;
	}
}
