package javelin.view.mappanel;

import java.awt.Graphics;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.controller.old.Game;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.overlay.Overlay;
import javelin.view.screen.BattleScreen;

/**
 * TODO remove {@link BattleMap} and rename this hierarchy
 * 
 * @author alex
 */
public class BattlePanel extends MapPanel {
	public static BattleState state = null;
	public static Overlay overlay = null;
	private boolean daylight;

	public BattlePanel(BattleState s) {
		super(s.map.length, s.map[0].length, Preferences.KEYTILEBATTLE);
		state = s.clonedeeply();
		String period = Javelin.app.fight.period;
		daylight = period.equals(Javelin.PERIODMORNING)
				|| period.equals(Javelin.PERIODNOON);
	}

	@Override
	protected Mouse getmouselistener() {
		return new BattleMouse(this);
	}

	@Override
	protected Tile newtile(int x, int y) {
		return new BattleTile(x, y, daylight, this);
	}

	@Override
	public void refresh() {
		super.refresh();
		final HashSet<Point> update = new HashSet<Point>(
				state.redTeam.size() + state.blueTeam.size());
		for (Combatant c : state.getCombatants()) {
			update.add(new Point(c.location[0], c.location[1]));
		}
		updatestate();
		for (Combatant c : state.getCombatants()) {
			update.add(new Point(c.location[0], c.location[1]));
		}
		if (!daylight) {
			calculatevision(update);
		}
		if (overlay != null) {
			update.addAll(overlay.affected);
		}
		for (Point p : update) {
			tiles[p.x][p.y].repaint();
		}
	}

	@Override
	public void paint(Graphics g) {
		updatestate();
		if (!daylight) {
			calculatevision(null);
		}
		super.paint(g);
	}

	private void calculatevision(final HashSet<Point> update) {
		final HashSet<Point> seen =
				Game.hero().combatant.calculatevision(state);
		for (Point p : seen) { // seen
			BattleTile t = (BattleTile) tiles[p.x][p.y];
			if (update != null && (!t.discovered || t.shrouded)) {
				update.add(p);
			}
			t.discovered = true;
			t.shrouded = false;
		}
		for (int x = 0; x < tiles.length; x++) { // unseen
			for (int y = 0; y < tiles[0].length; y++) {
				final BattleTile t = (BattleTile) tiles[x][y];
				if (!t.discovered || t.shrouded) {
					continue;
				}
				final Point p = new Point(x, y);
				if (!seen.contains(p)) {
					t.shrouded = true;
					if (update != null) {
						update.add(p);
					}
				}
			}
		}
	}

	void updatestate() {
		state = BattleScreen.active.map.getState().clonedeeply();
		BattleTile.panel = this;
	}

	@Override
	protected int gettilesize() {
		return Preferences.TILESIZEBATTLE;
	}
}
