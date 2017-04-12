package javelin.view.mappanel.dungeon;

import java.awt.Graphics;

import javelin.JavelinApp;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.view.Images;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.world.WorldPanel;

public class DungeonTile extends Tile {
	public DungeonTile(int xp, int yp, DungeonPanel p) {
		super(xp, yp, p.dungeon.visible[xp][yp]);
		addMouseListener(p.mouse);
	}

	@Override
	public void paint(Graphics g) {
		if (!discovered || Dungeon.active == null) {
			return;
		}
		draw(g, JavelinApp.context.gettile(x, y));
		if (Dungeon.active == null) {
			return;
		}
		final Feature f = Dungeon.active.getfeature(x, y);
		if (f != null && f.draw) {
			draw(g, Images.getImage(f.avatarfile));
		}
		if (Dungeon.active.herolocation.x == x
				&& Dungeon.active.herolocation.y == y) {
			Squad.active.updateavatar();
			draw(g, Squad.active.getimage());
		}
		if (WorldPanel.overlay != null) {
			WorldPanel.overlay.overlay(this, g);
		}
	}
}
