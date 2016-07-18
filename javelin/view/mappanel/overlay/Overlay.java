package javelin.view.mappanel.overlay;

import java.awt.Graphics;
import java.util.ArrayList;

import javelin.controller.Point;
import javelin.view.mappanel.BattlePanel;
import javelin.view.mappanel.BattleTile;
import javelin.view.screen.BattleScreen;

public abstract class Overlay {
	public ArrayList<Point> affected = new ArrayList<Point>();

	abstract public void overlay(BattleTile t, Graphics g);

	public void clear() {
		for (Point p : affected) {
			((BattlePanel) BattleScreen.active.mappanel).tiles[p.x][p.y]
					.repaint();
		}
		BattlePanel.overlay = null;
	}
}
