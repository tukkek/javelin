package javelin.view.mappanel.world;

import java.awt.Graphics;
import java.util.HashMap;

import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Location;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

public class WorldPanel extends MapPanel {

	static final HashMap<Point, WorldActor> ACTORS =
			new HashMap<Point, WorldActor>();
	public static final HashMap<Point, Location> DESTINATIONS =
			new HashMap<Point, Location>();

	public WorldPanel() {
		super(World.seed.map.length, World.seed.map[0].length,
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
		for (WorldActor a : WorldActor.getall()) {
			ACTORS.put(new Point(a.x, a.y), a);
			if (!(a instanceof Location)) {
				continue;
			}
			Location l = (Location) a;
			if (!l.discard) {
				DESTINATIONS.put(new Point(l.x, l.y), l);
			}
		}
	}

	@Override
	public void refresh() {
		if (initial) {
			// setLocation(0, 0);
			scroll.setSize(getBounds().getSize());
			// scroll.setLocation(0, 0);
			WorldActor s = Squad.active;
			zoom(0, true, s.x, s.y);
			center(s.x, s.y, true);
			scroll.setVisible(true);
		}
		super.refresh();
		repaint();
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
