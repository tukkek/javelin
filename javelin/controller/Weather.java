package javelin.controller;

import java.util.ArrayList;

import javelin.controller.db.Preferences;
import javelin.model.BattleMap;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import tyrant.mikera.engine.RPG;

/**
 * Manages game weather. Current types of weather are {@link #DRY},
 * {@link #RAIN} and {@link #STORM}.
 * 
 * @author alex
 */
public class Weather {
	static private Integer[] DISTRIBUTION = new Integer[] { 0, 0, 1, 2 };
	static private double[] RATIO = new double[] { 0.0, .1, .5 };
	static public int DRY = 0;
	static public int RAIN = 1;
	static public int STORM = 2;
	public static int current;

	static {
		read(0);
	}

	public static void weather() {
		if (Preferences.DEBUGWEATHER != null) {
			current = read(0);
			return;
		}
		int roll = RPG.pick(DISTRIBUTION);
		if (roll > current) {
			current += 1;
		} else if (roll < current) {
			current -= 1;
		}
	}

	public static void flood(BattleMap m, int maxflooding) {
		final double r = RATIO[Math.min(current, maxflooding)];
		if (r == 0.0) {
			return;
		}
		final ArrayList<Square> clear = new ArrayList<Square>();
		final BattleState state = m.getState();
		for (Square[] line : state.map) {
			for (Square s : line) {
				if (!s.obstructed && !s.blocked) {
					clear.add(s);
				}
			}
		}
		double spots = clear.size() * r;
		for (double i = 0.0; i < spots && !clear.isEmpty(); i += 1.0) {
			final int index = RPG.r(1, clear.size()) - 1;
			Square s = clear.get(index);
			clear.remove(index);
			s.flooded = true;
		}
		m.flood(state);
	}

	public static int read(int nowp) {
		if (Preferences.DEBUGWEATHER == null) {
			return nowp;
		}
		if (Preferences.DEBUGWEATHER.equals("dry")) {
			return DRY;
		}
		if (Preferences.DEBUGWEATHER.equals("rain")) {
			return RAIN;
		}
		if (Preferences.DEBUGWEATHER.equals("storm")) {
			return STORM;
		}
		throw new RuntimeException(
				"Unknown weather: " + Preferences.DEBUGWEATHER);
	}
}
