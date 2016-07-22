package javelin.view.mappanel.battle.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Overlay;
import javelin.view.mappanel.Tile;

public class BreathOverlay extends Overlay {
	private static final Border BORDER =
			BorderFactory.createLineBorder(Color.CYAN, MapPanel.tilesize / 10);

	public BreathOverlay(Set<Point> area) {
		affected.addAll(area);
	}

	@Override
	public void overlay(Tile t, Graphics g) {
		if (affected.contains(new Point(t.x, t.y))) {
			BORDER.paintBorder(t, g, 0, 0, MapPanel.tilesize,
					MapPanel.tilesize);
		}
	}
}
