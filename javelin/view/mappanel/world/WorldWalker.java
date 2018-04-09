package javelin.view.mappanel.world;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.town.Town;
import javelin.view.mappanel.battle.overlay.BattleWalker;
import javelin.view.screen.WorldScreen;

public class WorldWalker extends BattleWalker {
	HashSet<Point> safe = Town.getdistricts();
	protected boolean checksafe = true;

	public WorldWalker(Point from, Point to) {
		super(from, to, null, null);
	}

	@Override
	protected boolean end(float totalcost, boolean engaged,
			javelin.controller.walker.Step s) {
		return totalcost >= 1;
	}

	@Override
	protected float getcost(boolean engaged, javelin.controller.walker.Step s) {
		if (checksafe && safe.contains(new Point(s.x, s.y))) {
			return 0;
		}
		return Squad.active.move(false, Terrain.get(s.x, s.y), s.x, s.y)
				/ WorldScreen.HOURSPERENCOUNTER;
	}

	@Override
	protected boolean isengaged() {
		return false;
	}

	@Override
	protected boolean validatefinal() {
		if (WorldPanel.ACTORS.get(new Point(tox, toy)) != null) {
			return steps.size() == 1;
		}
		return checkwater(tox, toy);
	}

	@Override
	public boolean valid(int x, int y) {
		final Point p = new Point(x, y);
		return WorldPanel.ACTORS.get(p) == null && checkwater(x, y)
				&& WorldScreen.current.mappanel.tiles[x][y].discovered;
	}

	boolean checkwater(final int x, final int y) {
		return !Terrain.get(x, y).equals(Terrain.WATER) || Squad.active.swim();
	}

	@Override
	public String drawtext(float apcost) {
		if (!World.scenario.worldexploration) {
			return "";
		}
		return apcost > 1 ? "100%" : Math.round(apcost * 100) + "%";
	}

	@Override
	public Point resetlocation() {
		return new Point(Squad.active.x, Squad.active.y);
	}

	@Override
	public ArrayList<javelin.controller.walker.Step> walk() {
		ArrayList<javelin.controller.walker.Step> walk = super.walk();
		if (checksafe) {
			for (BattleStep s : steps) {
				if (safe.contains(new Point(s.x, s.y))) {
					s.safe = true;
				}
			}
		}
		return walk;
	}
}
