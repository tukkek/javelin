package javelin.controller.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javelin.controller.ai.cache.AiCache;
import javelin.controller.db.Properties;
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
			new ArrayList<Integer>();
	private static final int THINKINGPERIOD = Math.round(1000
			* Float.parseFloat(Properties.getString("ai.maxsecondsthinking")));
	public static int maxthreads;

	static public void determineprocessors() {
		int limit = Integer.parseInt(Properties.getString("ai.maxthreads"));
		int cores = Runtime.getRuntime().availableProcessors();
		maxthreads = cores;
		if (limit < maxthreads) {
			maxthreads = limit;
		}
		String message =
				"\n\nThe AI will use " + maxthreads + " of " + cores + " cores";
		if (AiCache.ENABLED) {
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
			for (int i = 0; i < maxthreads; i++) {
				AiThread.STARTED.add(new AiThread(state));
			}
			long sleepfor = THINKINGPERIOD - (now() - start);
			Thread.sleep(sleepfor >= 0 ? sleepfor : 0);
			// for (int i = 1; i < AiThread.STARTED.size(); i++) {
			// /*
			// * TODO would be nice to do this more properly so hanging threads
			// can be
			// * garbage-collected
			// */
			// AiThread.STARTED.get(i).interrupt();
			// }
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
