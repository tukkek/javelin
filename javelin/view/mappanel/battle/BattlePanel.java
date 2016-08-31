package javelin.view.mappanel.battle;

import java.awt.Graphics;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.Meld;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

/**
 * TODO remove {@link BattleMap} and rename this hierarchy
 * 
 * @author alex
 */
public class BattlePanel extends MapPanel {
	public boolean daylight;
	static public Combatant current = null;
	static public BattleState previousstate = null;
	static public BattleState state = null;

	public BattlePanel(BattleState s) {
		super(s.map.length, s.map[0].length, Preferences.KEYTILEBATTLE);
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
		updatestate();
		super.refresh();
		final HashSet<Point> update = new HashSet<Point>(
				Fight.state.redTeam.size() + Fight.state.blueTeam.size());
		for (Combatant c : Fight.state.getcombatants()) {
			update.add(new Point(c.location[0], c.location[1]));
		}
		for (Combatant c : previousstate.getcombatants()) {
			update.add(new Point(c.location[0], c.location[1]));
		}
		updatestate();
		for (Combatant c : Fight.state.getcombatants()) {
			update.add(new Point(c.location[0], c.location[1]));
		}
		if (!daylight) {
			calculatevision(update);
		}
		if (overlay != null) {
			update.addAll(overlay.affected);
		}
		if (Javelin.app.fight.meld) {
			for (Meld m : Fight.state.meld) {
				update.add(new Point(m.x, m.y));
			}
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
				Fight.state.clone(current).calculatevision(Fight.state);
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
		previousstate = state;
		state = Fight.state.clonedeeply();
		if (previousstate == null) {
			previousstate = state;
		}
		BattleTile.panel = this;
	}

	@Override
	protected int gettilesize() {
		return Preferences.TILESIZEBATTLE;
	}
}
