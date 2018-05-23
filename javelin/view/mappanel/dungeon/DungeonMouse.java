package javelin.view.mappanel.dungeon;

import java.awt.event.MouseEvent;

import javelin.controller.Point;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.Game;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.MoveOverlay;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldMouse;

public class DungeonMouse extends Mouse {
	public DungeonMouse(MapPanel panel) {
		super(panel);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (overrideinput()) {
			return;
		}
		if (!Game.userinterface.waiting) {
			return;
		}
		final Tile t = gettile(e);
		if (!t.discovered) {
			return;
		}
		if (e.getButton() == MouseEvent.BUTTON1 && WorldMouse.move()) {
			return;
		}
		super.mouseClicked(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!Game.userinterface.waiting) {
			return;
		}
		if (MapPanel.overlay != null) {
			MapPanel.overlay.clear();
		}
		MoveOverlay.cancel();
		final Tile t = gettile(e);
		if (!t.discovered) {
			return;
		}
		MoveOverlay
				.schedule(new MoveOverlay(new DungeonWalker(
						new Point(Dungeon.active.herolocation.x,
								Dungeon.active.herolocation.y),
						new Point(t.x, t.y))));
	}
}
