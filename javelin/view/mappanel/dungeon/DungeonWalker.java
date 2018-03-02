package javelin.view.mappanel.dungeon;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.Template;
import javelin.model.state.BattleState;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.Trap;
import javelin.view.mappanel.world.WorldWalker;
import javelin.view.screen.DungeonScreen;

public class DungeonWalker extends WorldWalker {
	public DungeonWalker(Point from, Point to) {
		super(from, to);
		checksafe = false;
	}

	@Override
	protected float getcost(boolean engaged, javelin.controller.walker.Step s) {
		return Dungeon.active.encounterratio;
	}

	@Override
	protected boolean validatefinal() {
		final Point target = new Point(targetx, targety);
		return !Dungeon.active.herolocation.equals(target)
				&& Dungeon.active.map[targetx][targety] != Template.WALL;
	}

	@Override
	protected boolean valid(int x, int y, BattleState state2) {
		if (!DungeonScreen.active.mappanel.tiles[x][y].discovered
				|| Dungeon.active.map[x][y] == Template.WALL) {
			return false;
		}
		final Feature f = Dungeon.active.getfeature(x, y);
		return f == null || f instanceof Trap;
	}

	@Override
	public Point resetlocation() {
		return Dungeon.active == null ? null : Dungeon.active.herolocation;
	}
}
