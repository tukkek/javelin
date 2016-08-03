package javelin.controller;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.controller.fight.Fight;
import javelin.model.state.BattleState;
import javelin.model.state.Square;
import javelin.model.world.Season;
import tyrant.mikera.engine.RPG;

/**
 * Manages game weather. Current types of weather are {@link #DRY},
 * {@link #RAIN} and {@link #STORM}.
 * 
 * @author alex
 */
public class Weather {
	static public int DRY = 0;
	static public int RAIN = 1;
	static public int STORM = 2;
	static private Integer[] DISTRIBUTION =
			new Integer[] { DRY, DRY, RAIN, STORM };
	static private double[] RATIO = new double[] { 0.0, .1, .5 };
	public static int current;

	static {
		read(0);
	}

	/**
	 * Changes the weather, possibly.
	 * 
	 * @see Season#getweather()
	 */
	public static void weather() {
		if (Preferences.DEBUGWEATHER != null) {
			current = read(0);
			return;
		}
		int roll =
				RPG.r(0, DISTRIBUTION.length - 1) + Season.current.getweather();
		if (roll < 0) {
			roll = 0;
		} else if (roll >= DISTRIBUTION.length) {
			roll = DISTRIBUTION.length - 1;
		}
		roll = DISTRIBUTION[roll];
		if (roll > current) {
			current += 1;
		} else if (roll < current) {
			current -= 1;
		}
	}

	public static void flood() {
		int level = Javelin.app.fight.flood();
		final double r = RATIO[level];
		if (r == 0.0) {
			return;
		}
		final ArrayList<Square> clear = new ArrayList<Square>();
		final BattleState state = Fight.state;
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
