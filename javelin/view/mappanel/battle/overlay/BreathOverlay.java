package javelin.view.mappanel.battle.overlay;

import java.awt.Canvas;
import java.awt.Color;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;

public class BreathOverlay extends Overlay {
	private static final Border BORDER = BorderFactory
			.createLineBorder(Color.CYAN, MapPanel.tilesize / 10);

	public BreathOverlay(Set<Point> area) {
		affected.addAll(area);
	}

	@Override
	public void overlay(Tile t) {
		if (affected.contains(new Point(t.x, t.y))) {
			Canvas canvas = BattleScreen.active.mappanel.canvas;
			Point p = t.getposition();
			BORDER.paintBorder(canvas, canvas.getGraphics(), p.x, p.y,
					MapPanel.tilesize, MapPanel.tilesize);
		}
	}
}
