package javelin.view.mappanel.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.HashMap;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.view.Images;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.BattlePanel;

public class WorldTile extends Tile {

	public static final HashMap<Point, Image> COASTLINES =
			new HashMap<Point, Image>(4);

	static {
		COASTLINES.put(new Point(-1, 0),
				Images.getImage("overlaycoastlineleft"));
		COASTLINES.put(new Point(+1, 0),
				Images.getImage("overlaycoastlineright"));
		COASTLINES.put(new Point(0, -1), Images.getImage("overlaycoastlineup"));
		COASTLINES.put(new Point(0, +1),
				Images.getImage("overlaycoastlinedown"));
	}

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
		if (Terrain.get(x, y).flooded()) {
			final Terrain t = Terrain.get(x, y);
			for (final Point p : COASTLINES.keySet()) {
				final int x = this.x + p.x;
				final int y = this.y + p.y;
				if (World.validatecoordinate(x, y)
						&& !Terrain.get(x, y).equals(t)) {
					draw(g, COASTLINES.get(p));
				}
			}
		}
		if (World.highways[x][y]) {
			paintroad(Color.LIGHT_GRAY, (Graphics2D) g);
		} else if (World.roads[x][y]) {
			paintroad(new Color(170, 130, 40), (Graphics2D) g);
		}
		final WorldActor a = WorldPanel.ACTORS.get(new Point(x, y));
		if (a != null) {
			if (a == Squad.active) {
				g.setColor(Color.GREEN);
				g.fillRect(0, 0, BattlePanel.tilesize, BattlePanel.tilesize);
			}
			draw(g, a.getimage());
		}
	}

	void paintroad(Color c, Graphics2D g) {
		g.setColor(c);
		g.setStroke(new BasicStroke(4));
		boolean any = false;
		final int center = WorldPanel.tilesize / 2;
		for (int deltax = -1; deltax <= +1; deltax++) {
			for (int deltay = -1; deltay <= +1; deltay++) {
				if (deltax == 0 && deltay == 0) {
					continue;
				}
				final int tox = x + deltax;
				final int toy = y + deltay;
				if (!World.validatecoordinate(tox, toy)) {
					continue;
				}
				if (World.roads[tox][toy] || World.highways[tox][toy]
						|| WorldPanel.DESTINATIONS
								.get(new Point(tox, toy)) != null) {
					any = true;
					g.drawLine(center, center, deltax * center + center,
							deltay * center + center);
				}
			}
		}
		if (!any) {
			g.drawLine(center, 0, center, WorldPanel.tilesize);
			g.drawLine(0, center, WorldPanel.tilesize, center);
		}
	}
}
