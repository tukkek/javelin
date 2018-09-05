package javelin.view.mappanel.world;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;

import javelin.controller.Point;
import javelin.model.world.location.town.Town;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;

public class DistrictOverlay extends Overlay {
	static final javax.swing.border.Border WHITEBORDER = BorderFactory
			.createLineBorder(Color.WHITE, 1);

	public DistrictOverlay(Town target) {
		affected.addAll(target.getdistrict().getarea());
	}

	@Override
	public void overlay(Tile t) {
		if (affected.contains(new Point(t.x, t.y))) {
			paint(t, BattleScreen.active.mappanel.getdrawgraphics());
		}
	}

	static public void paint(Tile t, Graphics g) {
		Point p = t.getposition();
		WHITEBORDER.paintBorder(BattleScreen.active.mappanel.canvas, g, p.x,
				p.y, MapPanel.tilesize, MapPanel.tilesize);
	}
}
