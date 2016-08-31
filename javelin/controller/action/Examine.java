package javelin.controller.action;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.exception.RepeatTurn;
import javelin.controller.fight.Fight;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.model.unit.Combatant;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.battle.overlay.TargetOverlay;
import javelin.view.screen.BattleScreen;
import javelin.view.screen.StatisticsScreen;

/**
 * Lets the player examine the surroundings and monsters.
 * 
 * @author alex
 */
public class Examine extends Action {
	final static HashMap<Character, Point> DIRECTIONS =
			new HashMap<Character, Point>();

	static {
		DIRECTIONS.put('8', new Point(0, -1));
		DIRECTIONS.put('2', new Point(0, 1));
		DIRECTIONS.put('4', new Point(-1, 0));
		DIRECTIONS.put('6', new Point(1, 0));
		DIRECTIONS.put('7', new Point(0, -1));
		DIRECTIONS.put('9', new Point(1, -1));
		DIRECTIONS.put('1', new Point(-1, 1));
		DIRECTIONS.put('3', new Point(1, 1));
	}

	/** Last unit to be examined. */
	static public Combatant lastlooked = null;

	/** Constructor. */
	public Examine() {
		super("examine", new String[] { "x" });
		allowwhileburrowed = true;
	}

	@Override
	public boolean perform(Combatant active) {
		doLook(BattleScreen.active);
		throw new RepeatTurn();
	}

	/** get location, initially place crosshairs at start */
	static public Point getTargetLocation(Point start, BattleScreen s) {
		startlooking(start, s);
		try {
			while (true) {
				final KeyEvent e = Game.getInput();
				if (e == null) {
					continue;
				}
				Point delta = new Point(0, 0);
				char key = convertkey(e);
				Point cursor = getcursor();
				if (DIRECTIONS.get(key) != null) {
					delta = DIRECTIONS.get(key);
				} else if (key == 'v') {
					new StatisticsScreen(
							Fight.state.getcombatant(cursor.x, cursor.y));
				} else if (key == 'q') {
					clearCursor();
					return null;
				} else {
					clearCursor();
					return cursor;
				}
				look(delta, cursor, s);
			}
		} finally {
			lastlooked = null;
			Game.messagepanel.clear();
		}
	}

	static void look(Point delta, Point cursor, BattleScreen s) {
		int x = checkbounds(cursor.x + delta.x, Fight.state.map.length);
		int y = checkbounds(cursor.y + delta.y, Fight.state.map[0].length);
		setCursor(x, y, s);
		s.mappanel.viewPosition(x, y);
		doLookPoint(getcursor(), s);
	}

	static void startlooking(Point start, BattleScreen s) {
		if (start == null) {
			Combatant active = Fight.state.next;
			start = new Point(active.location[0], active.location[1]);
		}
		setCursor(start.x, start.y, s);
		s.mappanel.viewPosition(start.x, start.y);
		doLookPoint(getcursor(), s);
		s.statuspanel.repaint();
	}

	static void clearCursor() {
		if (MapPanel.overlay != null) {
			MapPanel.overlay.clear();
		}
	}

	static Point getcursor() {
		TargetOverlay cursor = (TargetOverlay) MapPanel.overlay;
		return new Point(cursor.x, cursor.y);
	}

	static void setCursor(int x, int y, BattleScreen s) {
		clearCursor();
		MapPanel.overlay = new TargetOverlay(x, y);
		s.mappanel.refresh();
	}

	static void doLook(BattleScreen s) {
		doLookPoint(getTargetLocation(null, s), s);
	}

	static void doLookPoint(final Point p, BattleScreen s) {
		if (p == null) {
			return;
		}
		if (!s.mappanel.tiles[p.x][p.y].discovered) {
			lookmessage("Can't see", s);
			return;
		}
		lookmessage("", s);
		lastlooked = null;
		final BattleState state = Fight.state;
		final Combatant combatant = Fight.state.getcombatant(p.x, p.y);
		if (combatant != null) {
			lookmessage(describestatus(combatant, state), s);
			lastlooked = combatant;
		} else if (state.map[p.x][p.y].flooded) {
			lookmessage("Flooded", s);
		} else if (Javelin.app.fight.map.map[p.x][p.y].blocked) {
			lookmessage("Blocked", s);
		}
		s.statuspanel.repaint();
	}

	static void lookmessage(final String string, BattleScreen s) {
		s.messagepanel.clear();
		Game.message(
				"Examine: move the cursor over another combatant, press v to view character sheet.\n\n"
						+ string,
				Delay.NONE);
		Game.redraw();
	}

	static String describestatus(final Combatant combatant,
			final BattleState state) {
		final ArrayList<String> statuslist = combatant.liststatus(state);
		if (statuslist.isEmpty()) {
			return combatant.toString();
		}
		String description = combatant + " (";
		for (final String status : statuslist) {
			description += status + ", ";
		}
		return description.substring(0, description.length() - 2) + ")\n";
	}

	static char convertkey(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			return '8';
		case KeyEvent.VK_DOWN:
			return '2';
		case KeyEvent.VK_LEFT:
			return '4';
		case KeyEvent.VK_RIGHT:
			return '6';
		case KeyEvent.VK_HOME:
			return '7';
		case KeyEvent.VK_END:
			return '1';
		case KeyEvent.VK_PAGE_UP:
			return '9';
		case KeyEvent.VK_PAGE_DOWN:
			return '3';
		case KeyEvent.VK_ESCAPE:
			return 'q';
		default:
			return Character.toLowerCase(e.getKeyChar());
		}
	}

	static int checkbounds(int i, int upperbound) {
		if (i < 0) {
			return 0;
		}
		return i < upperbound ? i : upperbound - 1;
	}
}
