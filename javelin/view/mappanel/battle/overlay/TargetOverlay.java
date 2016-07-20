package javelin.view.mappanel.battle.overlay;

import java.awt.Graphics;
import java.awt.Image;

import javelin.controller.Point;
import javelin.view.Images;
import javelin.view.mappanel.Overlay;
import javelin.view.mappanel.battle.BattlePanel;
import javelin.view.mappanel.battle.BattleTile;
import javelin.view.screen.BattleScreen;

public class TargetOverlay extends Overlay {
	public static final Image target = Images.getImage("overlaytarget");

	public int x;
	public int y;

	private BattleTile t = null;

	public TargetOverlay(int x, int y) {
		this.x = x;
		this.y = y;
		affected.add(new Point(x, y));
		((BattlePanel) BattleScreen.active.mappanel).tiles[x][y].repaint();
	}

	@Override
	public void overlay(BattleTile t, Graphics g) {
		if (t.x == x && t.y == y) {
			g.drawImage(target, 0, 0, BattlePanel.tilesize,
					BattlePanel.tilesize, null);
			BattleScreen.active.centerscreen(x, y);
			affected.add(new Point(t.x, t.y));
		}
	}
}
