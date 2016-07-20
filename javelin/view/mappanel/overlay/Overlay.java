package javelin.view.mappanel.overlay;

import java.awt.Graphics;
import java.util.ArrayList;

import javelin.controller.Point;
import javelin.view.mappanel.BattlePanel;
import javelin.view.mappanel.BattleTile;
import javelin.view.mappanel.Tile;
import javelin.view.screen.BattleScreen;

public abstract class Overlay {
	public ArrayList<Point> affected = new ArrayList<Point>();

	abstract public void overlay(BattleTile t, Graphics g);

	public void clear() {
		BattlePanel.overlay = null;
		final Tile[][] tiles =
				((BattlePanel) BattleScreen.active.mappanel).tiles;
		for (Point p : affected) {
			tiles[p.x][p.y].repaint();
		}
		BattleScreen.active.mappanel.refresh();
	}
}
