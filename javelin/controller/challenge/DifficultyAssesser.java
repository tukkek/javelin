package javelin.controller.challenge;

import java.util.Map;
import java.util.TreeMap;

public class DifficultyAssesser {
	private static final int MAXIMUM = 5;
	private static final int MINIMUM = -13;
	static public Map<Integer, String> eldifference = new TreeMap<Integer, String>();
	static {
		eldifference.put(MINIMUM, "Irrelevant");
		for (int i = -12; i <= -9; i++) {
			eldifference.put(i, "Very easy");
		}
		for (int i = -8; i <= -5; i++) {
			eldifference.put(i, "Easy");
		}
		eldifference.put(-4, "Moderate");
		for (int i = -3; i <= 0; i++) {
			eldifference.put(i, "Difficult");
		}
		for (int i = 1; i <= 4; i++) {
			eldifference.put(i, "Very difficult");
		}
		eldifference.put(MAXIMUM, "Impossible");
	}

	public static String assess(final float player, final float enemy) {
		int difference = Math.round(enemy - player);
		if (difference <= MINIMUM) {
			difference = MINIMUM;
		} else if (difference >= MAXIMUM) {
			difference = MAXIMUM;
		}
		return eldifference.get(difference);
	}
}
