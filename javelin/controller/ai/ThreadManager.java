package javelin.controller.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javelin.controller.ai.cache.AiCache;
import javelin.controller.db.Preferences;
import javelin.model.state.BattleState;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

/**
 * Managers{@link AiThread}s.
 * 
 * @author alex
 */
public class ThreadManager {

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
			Game.message("Fatal error: " + ERROR.getMessage(), null,
					Delay.NONE);
			ERROR.printStackTrace();
			throw new RuntimeException(ERROR);
		}
		int deepest = AiThread.FINISHED.descendingKeySet().first();
		System.out.println((now() - start) / 1000f + " seconds elapsed. Depth: "
				+ deepest);
		BATTLERECORD.add(deepest);
		return AiThread.FINISHED.get(deepest);
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
