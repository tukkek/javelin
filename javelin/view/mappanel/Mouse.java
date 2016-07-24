package javelin.view.mappanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javelin.JavelinApp;
import javelin.controller.Point;
import javelin.controller.old.Game;
import javelin.view.screen.BattleScreen;

public abstract class Mouse extends MouseAdapter {
	MapPanel panel;

	public Mouse(MapPanel panel) {
		this.panel = panel;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == e.BUTTON3) {
			Tile t = (Tile) e.getSource();
			BattleScreen.active.mappanel.center(t.x, t.y, true);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Point p = JavelinApp.context.getherolocation();
		panel.zoom(-e.getWheelRotation(), false, p.x, p.y);
	}

	/**
	 * @return Subclasses should call this at the beggining of
	 *         {@link #mouseClicked(MouseEvent)} and return without further
	 *         action if <code>true</code>.
	 */
	public boolean overrideinput() {
		if (Game.userinterface.waiting) {
			return false;
		}
		Game.simulateKey('\n');
		return true;
	}
}
