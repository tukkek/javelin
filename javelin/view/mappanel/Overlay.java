package javelin.view.mappanel;

import java.awt.Graphics;
import java.util.ArrayList;

import javelin.controller.Point;
import javelin.view.screen.BattleScreen;

public abstract class Overlay {
	public ArrayList<Point> affected = new ArrayList<Point>();

	abstract public void overlay(Tile t, Graphics g);

	public void clear() {
		MapPanel.overlay = null;
		final Tile[][] tiles = ((MapPanel) BattleScreen.active.mappanel).tiles;
		for (Point p : affected) {
			tiles[p.x][p.y].repaint();
		}
		BattleScreen.active.mappanel.refresh();
	}
}
