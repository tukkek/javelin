package javelin.view.mappanel;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.view.mappanel.battle.overlay.BattleWalker;
import javelin.view.mappanel.battle.overlay.BattleWalker.BattleStep;
import javelin.view.screen.BattleScreen;

public class MoveOverlay extends Overlay {
	static final javax.swing.border.Border WHITEBORDER = BorderFactory
			.createLineBorder(Color.WHITE, 3);
	static final javax.swing.border.Border GREENBORDER = BorderFactory
			.createLineBorder(Color.GREEN, 3);
	static final javax.swing.border.Border REDBORDER = BorderFactory
			.createLineBorder(Color.RED, 3);

	public BattleWalker path;

	public MoveOverlay(BattleWalker mover) {
		path = mover;
	}

	public void walk() {
		path.walk();
		try {
			for (BattleStep step : path.steps) {
				affected.add(new Point(step.x, step.y));
				BattleScreen.active.mappanel.tiles[step.x][step.y].repaint();
			}
		} catch (IndexOutOfBoundsException e) {
			affected.clear();
		}
	}

	@Override
	public void overlay(Tile t) {
		Graphics g = BattleScreen.active.mappanel.getdrawgraphics();
		Point p = t.getposition();
		for (BattleStep s : path.steps) {
			if (t.x == s.x && t.y == s.y) {
				final boolean partial = !s.engaged && s.totalcost <= .5f;
				Border border;
				if (s.safe) {
					border = WHITEBORDER;
				} else {
					border = partial ? GREENBORDER : REDBORDER;
				}
				border.paintBorder(BattleScreen.active.mappanel.canvas, g, p.x,
						p.y, MapPanel.tilesize, MapPanel.tilesize);
				if (!s.safe) {
					g.setColor(partial ? Color.GREEN : Color.RED);
					g.drawString(path.drawtext(s.totalcost), p.x + 5,
							p.y + MapPanel.tilesize - 5);
				}
			}
		}
	}

	static Timer moveschedule;

	synchronized public static void schedule(final MoveOverlay overlay) {
		if (moveschedule != null) {
			moveschedule.cancel();
		}
		moveschedule = new Timer();
		moveschedule.schedule(new TimerTask() {
			@Override
			public void run() {
				MapPanel.overlay = overlay;
				overlay.walk();
				moveschedule.cancel();
				BattleScreen.active.mappanel.refresh();
			}
		}, 100);
	}

	synchronized public static void cancel() {
		if (moveschedule != null) {
			moveschedule.cancel();
		}
	}

	public void reset() {
		path.reset();
		affected.clear();
	}
}
