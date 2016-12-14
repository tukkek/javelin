package javelin.view.mappanel.world;

import java.util.ArrayList;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.state.BattleState;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.mappanel.battle.overlay.BattleMover;
import javelin.view.screen.WorldScreen;

public class WorldMover extends BattleMover {
	HashSet<Point> safe = Town.getdistricts();
	protected boolean checksafe = true;

	public WorldMover(Point from, Point to) {
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
		if (WorldPanel.ACTORS.get(new Point(targetx, targety)) != null) {
			return steps.size() == 1;
		}
		return true;
	}

	@Override
	protected boolean valid(int x, int y, BattleState state2) {
		final Terrain t = Terrain.get(x, y);
		if (t.equals(Terrain.WATER) && !Squad.active.swim()) {
			return false;
		}
		return WorldPanel.ACTORS.get(new Point(x, y)) == null;
	}

	@Override
	public String drawtext(float apcost) {
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
			for (Step s : steps) {
				if (safe.contains(new Point(s.x, s.y))) {
					s.safe = true;
				}
			}
		}
		return walk;
	}
}
