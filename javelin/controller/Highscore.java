package javelin.controller;

import javelin.Javelin;
import javelin.model.world.World;
import javelin.view.screen.WorldScreen;

public class Highscore {

	public Highscore() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sets the highscore and...
	 *
	 * @return a message with previous and current score.
	 */
	public static String record() {
		if (!World.scenario.record) {
			return "";
		}
		final long stored = Highscore.gethighscore();
		final long current = WorldScreen.currentday();
		String message = "Previous record: " + stored;
		if (stored < current) {
			message += "\nNew record: " + current + "!";
			Highscore.sethighscore(current);
		} else {
			message += "\nCurrent game: " + current;
		}
		return message;
	}

	/**
	 * @param score
	 *            Updates the highscore record with this value.
	 */
	public static void sethighscore(final long score) {
		Javelin.RECORD.putLong("record", score);
	}

	/**
	 * @return The current highscore value.
	 */
	public static long gethighscore() {
		return Javelin.RECORD.getLong("record", 0);
	}

}
