package javelin.view.mappanel.world;

import java.awt.Color;
import java.awt.Graphics;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.model.unit.Squad;
import javelin.model.world.WorldActor;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.BattlePanel;

public class WorldTile extends Tile {
	public WorldTile(int xp, int yp, WorldPanel p) {
		super(xp, yp, false);
		addMouseListener(p.mouse);
	}

	@Override
	public void paint(Graphics g) {
		if (!discovered) {
			return;
		}
		draw(g, JavelinApp.context.gettile(x, y));
		final WorldActor a = WorldPanel.ACTORS.get(new Point(x, y));
		if (a != null) {
			if (a == Squad.active) {
				g.setColor(Color.GREEN);
				g.fillRect(0, 0, BattlePanel.tilesize, BattlePanel.tilesize);
			}
			draw(g, a.getimage());
		}
	}
}
