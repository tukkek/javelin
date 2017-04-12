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
	Dungeon dungeon;

	public DungeonScreen(Dungeon dungeon) {
		super(false);
		this.dungeon = dungeon;
		open();
	}

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

	@Override
	public boolean explore(float hoursellapsed, boolean encounter) {
		try {
			if (encounter) {
				RandomEncounter.encounter(Dungeon.ENCOUNTERRATIO);
			}
		} catch (StartBattle e) {
			throw e;
		}
		return !dungeon.hazard();
	}

	@Override
	public boolean react(Actor actor, int x, int y) {
		int searchroll = Squad.active.search();
		for (Feature f : new ArrayList<Feature>(dungeon.features)) {
			if (f.x == x && f.y == y) {
				boolean activated = f.activate();
				if (activated && f.remove) {
					f.remove();
					DungeonScreen.dontenter = !f.enter;
					DungeonScreen.stopmovesequence = f.stop;
				}
				return activated;
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
		return !dungeon.walls.contains(new javelin.controller.Point(x, y));
	}

	@Override
	public void updatelocation(int x, int y) {
		dungeon.herolocation = new Point(x, y);
	}

	@Override
	public void view(int xp, int yp) {
		for (int x = -1; x <= +1; x++) {
			for (int y = -1; y <= +1; y++) {
				try {
					dungeon.setvisible(dungeon.herolocation.x + x,
							dungeon.herolocation.y + y);
				} catch (ArrayIndexOutOfBoundsException e) {
					continue;
				}
			}
		}
	}

	@Override
	public Image gettile(int x, int y) {
		return Images.getImage(dungeon.walls.contains(new Point(x, y))
				? dungeon.wall : dungeon.floor);
	}

	@Override
	public Fight encounter() {
		return dungeon.encounter();
	}

	@Override
	protected MapPanel getmappanel() {
		return new DungeonPanel(dungeon);
	}

	@Override
	public boolean validatepoint(int tox, int toy) {
		return 0 <= tox && tox < Dungeon.SIZE && 0 <= toy && toy < Dungeon.SIZE;
	}

	@Override
	public Point getherolocation() {
		return dungeon.herolocation;
	}

	@Override
	protected HashSet<Point> getdiscoveredtiles() {
		return dungeon.discovered;
	}
}
