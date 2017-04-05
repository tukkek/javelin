package javelin.view.mappanel.world;

import java.awt.Graphics;
import java.util.HashMap;

import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.Actor;
import javelin.model.world.location.Location;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

public class WorldPanel extends MapPanel {

	static final HashMap<Point, Actor> ACTORS = new HashMap<Point, Actor>();
	public static final HashMap<Point, Location> DESTINATIONS = new HashMap<Point, Location>();

	public WorldPanel() {
		super(World.getseed().map.length, World.getseed().map[0].length,
				Preferences.KEYTILEWORLD);
	}

	@Override
	protected Mouse getmouselistener() {
		return new WorldMouse(this);
	}

	@Override
	protected int gettilesize() {
		return Preferences.TILESIZEWORLD;
	}

	@Override
	protected Tile newtile(int x, int y) {
		return new WorldTile(x, y, this);
	}

	@Override
	public void paint(Graphics g) {
		updateactors();
		super.paint(g);
	}

	void updateactors() {
		DESTINATIONS.clear();
		ACTORS.clear();
		for (Actor a : World.getall()) {
			ACTORS.put(new Point(a.x, a.y), a);
			if (!(a instanceof Location)) {
				continue;
			}
			Location l = (Location) a;
			if (l.link) {
				DESTINATIONS.put(new Point(l.x, l.y), l);
			}
		}
	}

	@Override
	public void refresh() {
		if (initial) {
			resize(this, Squad.active.x, Squad.active.y);
		}
		super.refresh();
		repaint();
	}

	static public void resize(MapPanel p, int x, int y) {
		p.scroll.setSize(p.getBounds().getSize());
		p.zoom(0, true, x, y);
		p.center(x, y, true);
		p.scroll.setVisible(true);
	}

	@Override
	public void repaint() {
		/*
		 * For some reasone super.repaint() isn't calling #paint at all, so
		 * let's do it manually
		 */
		updateactors();
		for (Tile[] ts : tiles) {
			for (Tile t : ts) {
				if (t.discovered) {
					t.repaint();
				}
			}
		}
	}

	@Override
	public void init() {
		super.init();
		scroll.setVisible(false);
	}
}
