package javelin.view.mappanel.world;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.BorderFactory;

import javelin.controller.Point;
import javelin.model.world.location.town.Town;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Overlay;
import javelin.view.mappanel.Tile;

public class DistrictOverlay extends Overlay {
	static final javax.swing.border.Border WHITEBORDER = BorderFactory
			.createLineBorder(Color.WHITE, 1);

	public DistrictOverlay(Town target) {
		affected.addAll(target.getdistrict().getarea());
	}

	@Override
	public void overlay(Tile t, Graphics g) {
		if (affected.contains(new Point(t.x, t.y))) {
			paint(t, g);
		}
	}

	static public void paint(Tile t, Graphics g) {
		WHITEBORDER.paintBorder(t, g, 0, 0, MapPanel.tilesize,
				MapPanel.tilesize);
	}
}
