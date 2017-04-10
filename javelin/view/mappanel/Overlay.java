package javelin.view.mappanel;

import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

import javelin.controller.Point;
import javelin.view.screen.BattleScreen;

public abstract class Overlay {
	public ArrayList<Point> affected = new ArrayList<Point>();

	abstract public void overlay(Tile t, Graphics g);

	public void clear() {
		MapPanel.overlay = null;
		final Tile[][] tiles = BattleScreen.active.mappanel.tiles;
		for (Point p : affected) {
			try {
				tiles[p.x][p.y].repaint();
			} catch (IndexOutOfBoundsException e) {
				continue;// TODO
			}
		}
		BattleScreen.active.mappanel.refresh();
	}

	/**
	 * Draws image on given tile and adds it to #affected.
	 */
	protected void draw(Tile t, Graphics g, Image i) {
		g.drawImage(i, 0, 0, MapPanel.tilesize, MapPanel.tilesize, null);
		affected.add(new Point(t.x, t.y));
	}

	public void refresh(MapPanel mappanel) {
		for (Point p : affected) {
			mappanel.tiles[p.x][p.y].repaint();
		}
	}
}
