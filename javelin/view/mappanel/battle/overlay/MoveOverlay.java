package javelin.view.mappanel.battle.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;

import javelin.controller.Point;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Overlay;
import javelin.view.mappanel.Tile;
import javelin.view.mappanel.battle.overlay.Mover.Step;
import javelin.view.screen.BattleScreen;

public class MoveOverlay extends Overlay {
	static final javax.swing.border.Border GREENBORDER =
			BorderFactory.createLineBorder(Color.GREEN, 3);
	static final javax.swing.border.Border REDBORDER =
			BorderFactory.createLineBorder(Color.RED, 3);

	public Mover path;

	public MoveOverlay(Mover mover) {
		path = mover;
	}

	void walk() {
		path.walk();
		for (Step step : path.steps) {
			affected.add(new Point(step.x, step.y));
			((MapPanel) BattleScreen.active.mappanel).tiles[step.x][step.y]
					.repaint();
		}
	}

	@Override
	public void overlay(Tile t, Graphics g) {
		for (Step s : path.steps) {
			if (t.x == s.x && t.y == s.y) {
				final boolean partial = !s.engaged && s.apcost <= .5f;
				(partial ? GREENBORDER : REDBORDER).paintBorder(t, g, 0, 0,
						MapPanel.tilesize, MapPanel.tilesize);
				g.setColor(partial ? Color.GREEN : Color.RED);
				g.drawString(path.drawtext(s.apcost), 5, MapPanel.tilesize - 5);
			}
		}
	}

	static Timer moveschedule;

	public static void schedule(final MoveOverlay overlay) {
		moveschedule = new Timer();
		moveschedule.schedule(new TimerTask() {
			@Override
			public void run() {
				MapPanel.overlay = overlay;
				overlay.walk();
				moveschedule.cancel();
				((MapPanel) BattleScreen.active.mappanel).refresh();
			}
		}, 100);
	}

	public static void cancel() {
		if (moveschedule != null) {
			moveschedule.cancel();
		}
	}
}
