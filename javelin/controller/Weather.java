package javelin.controller;

import java.util.ArrayList;

import javelin.Javelin;
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
	public static int now;

	static {
		read(0);
	}

	public static void weather() {
		if (Javelin.DEBUGWEATHER != null) {
			now = Javelin.DEBUGWEATHER;
			return;
		}
		int roll = RPG.pick(DISTRIBUTION);
		if (roll > now) {
			now += 1;
		} else if (roll < now) {
			now -= 1;
		}
	}

	public static void flood(BattleMap m, int maxflooding) {
		final double r = RATIO[Math.min(now, maxflooding)];
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

	public static void read(int nowp) {
		now = Javelin.DEBUGWEATHER == null ? nowp : Javelin.DEBUGWEATHER;
	}
}
