package javelin.view.mappanel.battle.overlay;

import java.awt.Graphics;
import java.awt.Image;

import javelin.controller.Point;
import javelin.view.Images;
import javelin.view.mappanel.Overlay;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.BattleTile;
import javelin.view.screen.BattleScreen;

public class TargetOverlay extends Overlay {
	public static final Image TARGET = Images.getImage("overlaytarget");

	public int x;
	public int y;

	private BattleTile t = null;

	public TargetOverlay(int x, int y) {
		this.x = x;
		this.y = y;
		affected.add(new Point(x, y));
		BattleScreen.active.mappanel.tiles[x][y].repaint();
	}

	@Override
	public void overlay(Tile t, Graphics g) {
		if (t.x == x && t.y == y) {
			draw(t, g, TARGET);
			BattleScreen.active.centerscreen(x, y);
		}
	}
}
