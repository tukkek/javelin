package javelin.model.world;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Squad;
import tyrant.mikera.engine.RPG;

/**
 * Four seasons. The starting season for a world is random and each seasons
 * lasts for {@value #SEASONDURATION} days.
 * 
 * Different seasons (and times of day) have different effects on
 * {@link Terrain} hazards and events.
 * 
 * @author alex
 * @see Javelin#getDayPeriod()
 */
public enum Season {
	/** Season of blooming nature. */
	SPRING,
	/** Season of peak heat. */
	SUMMER,
	/** Dry season. */
	AUTUMN,
	/** Cold season. */
	WINTER;

	/** How long each season lass. */
	public static int SEASONDURATION = 100;

	/** Current game {@link World} season. */
	public static Season current =
			Season.values()[RPG.r(0, Season.values().length - 1)];
	/** When the next season starts. */
	public static int endsat = SEASONDURATION;

	/**
	 * Changes the season, if necessary. Should be called once per day at least.
	 * 
	 * @param day
	 *            Current day, starting from day 1.
	 * @see Squad#hourselapsed
	 */
	public static void change(int day) {
		if (day >= endsat) {
			endsat += endsat;
			int next = current.ordinal() + 1;
			current = values()[next < values().length ? next : 0];
		}
	}

	@Override
	public String toString() {
		String name = super.toString();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}
}