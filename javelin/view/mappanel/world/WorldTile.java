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
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;

public class WorldTile extends Tile {

	public static final HashMap<Point, Image> COASTLINES = new HashMap<Point, Image>(
			4);

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
		if (World.seed.highways[x][y]) {
			paintroad(Color.LIGHT_GRAY, (Graphics2D) g);
		} else if (World.seed.roads[x][y]) {
			paintroad(new Color(170, 130, 40), (Graphics2D) g);
		}
		final WorldActor a = WorldPanel.ACTORS.get(new Point(x, y));
		if (a != null) {
			drawactor(g, a);
		}
		if (WorldPanel.overlay != null) {
			WorldPanel.overlay.overlay(this, g);
		}
	}

	void drawactor(final Graphics g, final WorldActor a) {
		if (a == Squad.active) {
			g.setColor(Color.GREEN);
			g.fillRect(0, 0, MapPanel.tilesize, MapPanel.tilesize);
			if (Squad.active.getdistrict() != null) {
				DistrictOverlay.paint(this, g);
			}
		}
		draw(g, a.getimage());
		if (a.getrealmoverlay() != null) {
			g.setColor(a.getrealmoverlay().getawtcolor());
			g.fillRect(0, WorldPanel.tilesize - 5, WorldPanel.tilesize, 5);
		}
		final Location l = a instanceof Location ? (Location) a : null;
		if (l == null) {
			return;
		}
		if (l.drawgarisson()) {
			draw(g, Images.HOSTILE);
		}
		if (l.hascrafted()) {
			draw(g, Images.CRAFTING);
		}
		if (l.hasupgraded()) {
			draw(g, Images.UPGRADING);
		}
		if (l.isworking()) {
			draw(g, Images.LABOR);
		}
		final Town t = l instanceof Town ? (Town) l : null;
		if (t != null && !t.ishostile() && t.isworking()) {
			draw(g, Images.LABOR);
		}
	}

	void paintroad(Color c, Graphics2D g) {
		g.setColor(c);
		g.setStroke(new BasicStroke(4));
		final int center = WorldPanel.tilesize / 2;
		boolean any = false;
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
				if (World.seed.roads[tox][toy] || World.seed.highways[tox][toy]
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
