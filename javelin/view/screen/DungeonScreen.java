package javelin.view.screen;

import java.awt.Image;
import java.util.ArrayList;

import javelin.controller.Point;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.RandomEncounter;
import javelin.controller.old.Game;
import javelin.model.BattleMap;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.Trap;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.dungeon.DungeonPanel;
import tyrant.mikera.engine.Thing;

/**
 * Shows the inside of a {@link Dungeon}.
 * 
 * @author alex
 */
public class DungeonScreen extends WorldScreen {
	/** TODO hack */
	public static boolean dontenter = false;
	public static boolean stopmovesequence = false;
	public static boolean updatelocation = true;

	/** Exhibits a dungeon. */
	public DungeonScreen(BattleMap map) {
		super(null);
	}

	@Override
	public boolean explore(float hoursellapsed, boolean encounter) {
		try {
			if (encounter) {
				RandomEncounter.encounter(Dungeon.ENCOUNTERRATIO);
			}
		} catch (StartBattle e) {
			map.removeThing(Game.hero());
			throw e;
		}
		return !Dungeon.active.hazard();
	}

	@Override
	public boolean react(WorldActor actor, int x, int y) {
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
	public void view(Thing h) {
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
	public Thing gethero() {
		Squad.active.updateavatar();
		Thing hero = Game.hero();
		hero.combatant = Squad.active.visual.combatant;
		hero.x = Dungeon.active.herolocation.x;
		hero.y = Dungeon.active.herolocation.y;
		return Game.hero();
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
	protected void humanTurn() {
		super.humanTurn();
	}

	@Override
	public boolean validatepoint(int tox, int toy) {
		return 0 <= tox && tox < Dungeon.SIZE && 0 <= toy && toy < Dungeon.SIZE;
	}
}
