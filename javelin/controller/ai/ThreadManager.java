package javelin.controller.ai;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javelin.controller.db.Preferences;
import javelin.model.state.BattleState;
import tyrant.mikera.tyrant.Game;
import tyrant.mikera.tyrant.Game.Delay;

public class ThreadManager {

	private static final int THINKINGPERIOD = Math.round(1000 * Float
			.parseFloat(Preferences.getString("ai.maxsecondsthinking")));
	public static int maxthreads;

	static public void determineprocessors() {
		int limit = Integer.parseInt(Preferences.getString("ai.maxthreads"));
		int cores = Runtime.getRuntime().availableProcessors();
		maxthreads = cores;
		if (limit < maxthreads) {
			maxthreads = limit;
		}
		System.out.println("The AI will use " + maxthreads + " of " + cores
				+ " cores");
	}

	static final Thread MAIN = Thread.currentThread();
	public static Exception ERROR;

	public static List<ChanceNode> think(BattleState state) {
		final long start = now();
		AiThread.depthincremeneter = 1;
		AiThread.reset();
		for (int i = 0; i < maxthreads; i++) {
			AiThread.STARTED.add(new AiThread(state.clone()));
		}
		try {
			Thread.sleep(THINKINGPERIOD);
			if (AiThread.FINISHED.isEmpty()) {
				AiThread.STARTED.get(0).join();
			}
			for (Thread t : new ArrayList<Thread>(AiThread.STARTED)) {
				t.interrupt();
				t.join();
			}
			AiThread.GROUP.interrupt();
		} catch (InterruptedException e) {
			Game.message("Fatal error: " + ERROR.getMessage(), null, Delay.NONE);
			throw new RuntimeException(ERROR);
		}
		final float seconds = now() - start;
		int deepest = AiThread.FINISHED.descendingKeySet().first();
		System.out.println(seconds / 1000 + " seconds elapsed. Depth: "
				+ deepest);
		return AiThread.FINISHED.get(deepest).result;
	}

	static private long now() {
		return new Date().getTime();
	}
}
