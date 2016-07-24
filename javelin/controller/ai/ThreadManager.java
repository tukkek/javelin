package javelin.controller.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import javelin.Javelin;
import javelin.controller.action.world.ShowOptions;
import javelin.controller.ai.cache.AiCache;
import javelin.controller.db.Preferences;
import javelin.controller.old.Game;
import javelin.controller.old.Game.Delay;
import javelin.model.state.BattleState;
import javelin.view.screen.BattleScreen;

/**
 * Managers{@link AiThread}s.
 * 
 * @author alex
 */
public class ThreadManager {

	private static final String PERFORMANCESOLUTION =
			"The maximum number of AI threads has been lowered to %1$d.\n"
					+ "This reduces the AI thinking power but increases speed.\n"
					+ "You may need to go through this step a couple of times.\n\n"
					+ "If you don't want to lose thinking power you can try\n"
					+ "raising the thinking time manually via the Options screen.";
	private static final String PERFORMANCENOTIFICATION =
			"Your computer seems to be having trouble thinking fast enough.\n\n"
					+ "The current configured thinking time is ~%1$d seconds.\n" //
					+ "The current computer move has taken ~%2$d seconds to complete.\n\n"
					+ "Press Yes to attempt an automatic solution.\n"
					+ "Press No to ignore this message until the game is restarted.\n"
					+ "Press Cancel to never see this message again.\n\n"
					+ "You can also press %3$c after closing this dialog to configure your preferences manually.";
	private static final ArrayList<Integer> BATTLERECORD =
			new ArrayList<Integer>();;

	static public void determineprocessors() {
		String message =
				"\n\nThe AI will use " + Preferences.MAXTHREADS + " cores";
		if (Preferences.AICACHEENABLED) {
			message += " (cache enabled)";
		}
		System.out.println(message);
		System.out.println();
	}

	static final Thread MAIN = Thread.currentThread();
	public static Exception ERROR;
	public static boolean working;
	/**
	 * Increment by 1 if computation was done within proximity of
	 * {@link Preferences#MAXMILISECONDSTHINKING} or decrement by 1 otherwise.
	 * {@link Integer#MIN_VALUE} means no performance monitoring.
	 */
	public static int performance = 0;

	public static List<ChanceNode> think(BattleState state) {
		final long start;
		try {
			working = true;
			interrupt();
			TemperatureManager.cooldown();
			start = now();
			AiCache.clear();
			AiThread.depthincremeneter = 1;
			AiThread.reset();
			for (int i = 0; i < Preferences.MAXTHREADS; i++) {
				AiThread.STARTED.add(new AiThread(state));
			}
			long sleepfor =
					Preferences.MAXMILISECONDSTHINKING - (now() - start);
			Thread.sleep(sleepfor >= 0 ? sleepfor : 0);
			while (AiThread.FINISHED.isEmpty()) {
				if (ThreadManager.ERROR != null) {
					throw ThreadManager.ERROR;
				}
				Thread.sleep(500);
			}
			interrupt();
			working = false;
		} catch (Exception e) {
			interrupt();
			working = false;
			Game.message("Fatal error: " + ERROR.getMessage(), Delay.NONE);
			ERROR.printStackTrace();
			throw new RuntimeException(ERROR);
		}
		int deepest = AiThread.FINISHED.descendingKeySet().first();
		float miliseconds = (now() - start);
		checkperformance(miliseconds);
		System.out.println(
				(miliseconds / 1000f) + " seconds elapsed. Depth: " + deepest);
		BATTLERECORD.add(deepest);
		return AiThread.FINISHED.get(deepest);
	}

	static void checkperformance(float miliseconds) {
		if (performance == Integer.MIN_VALUE || Preferences.MAXTHREADS == 1) {
			return;
		}
		performance += miliseconds < Preferences.MAXMILISECONDSTHINKING + 1000
				? +1 : -1;
		if (performance >= -9) {
			return;
		}
		String message = String.format(PERFORMANCENOTIFICATION,
				(Preferences.MAXMILISECONDSTHINKING / 1000),
				Math.round(miliseconds / 1000),
				ShowOptions.getsingleton().morekeys[0].charAt(0));
		int choice = JOptionPane.showConfirmDialog(BattleScreen.active, message,
				"Performance problem detected",
				JOptionPane.YES_NO_CANCEL_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			performance = 0;
			int to = Preferences.MAXTHREADS - 1;
			Preferences.setoption(Preferences.KEYMAXTHREADS, to);
			JOptionPane.showMessageDialog(BattleScreen.active,
					String.format(PERFORMANCESOLUTION, to));
		} else if (choice == JOptionPane.NO_OPTION) {
			performance = Integer.MIN_VALUE;
		} else if (choice == JOptionPane.CANCEL_OPTION) {
			performance = Integer.MIN_VALUE;
			Preferences.savefile(Preferences.getfile() + "\n"
					+ Preferences.KEYCHECKPERFORMANCE + "=false");
		} else if (choice != -1 && Javelin.DEBUG) {
			throw new RuntimeException("#performancereply " + choice);
		}
	}

	static boolean interrupting;

	public static void interrupt() {
		interrupting = true;
		AiThread.group.interrupt();
		for (Thread t : new ArrayList<Thread>(AiThread.STARTED)) {
			// t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		AiThread.group.destroy();
		AiThread.group = AiThread.regroup();
		interrupting = false;
	}

	static private long now() {
		return new Date().getTime();
	}

	static public void printbattlerecord() {
		if (BATTLERECORD.isEmpty()) {
			return;
		}
		Collections.sort(BATTLERECORD);
		System.out.println("Median depth for battle: "
				+ BATTLERECORD.get(BATTLERECORD.size() / 2));
		System.out.println();
		BATTLERECORD.clear();
	}
}
