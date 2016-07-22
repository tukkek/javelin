package javelin.view.mappanel.dungeon;

import javelin.controller.Point;
import javelin.model.state.BattleState;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.Trap;
import javelin.view.mappanel.world.WorldMover;

public class DungeonMover extends WorldMover {
	private static final float ENCOUNTERCHANCE = 1 / Dungeon.ENCOUNTERRATIO;

	public DungeonMover(Point from, Point to) {
		super(from, to);
	}

	@Override
	protected float getcost(boolean engaged, javelin.controller.walker.Step s) {
		return Dungeon.ENCOUNTERRATIO;
	}

	@Override
	protected boolean validatefinal() {
		final Point target = new Point(targetx, targety);
		return !Dungeon.active.herolocation.equals(target)
				&& !Dungeon.active.walls.contains(target);
	}

	@Override
	protected boolean valid(int x, int y, BattleState state2) {
		if (Dungeon.active.walls.contains(new Point(x, y))) {
			return false;
		}
		final Feature f = Dungeon.active.getfeature(x, y);
		return f == null || f instanceof Trap;
	}

	@Override
	public Point resetlocation() {
		return new Point(Dungeon.active.herolocation.x,
				Dungeon.active.herolocation.y);
	}
}
