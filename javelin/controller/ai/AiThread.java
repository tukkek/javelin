package javelin.controller.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javelin.model.state.BattleState;

public class AiThread extends Thread {
	public static final ThreadGroup GROUP = new ThreadGroup("JavelinAI");
	static final ArrayList<Thread> STARTED = new ArrayList<Thread>();
	public static final TreeMap<Integer, AiThread> FINISHED = new TreeMap<Integer, AiThread>();
	static int depthincremeneter;

	public BattleState state;
	public int depth;
	List<ChanceNode> result;

	public AiThread(BattleState state) {
		super(GROUP, (Runnable) null);
		this.state = state;
		setdepth();
		start();
	}

	@Override
	public void run() {
		try {
			result = new BattleAi(depth).alphaBetaSearch(state);
			onend();
		} catch (InterruptedException e) {
			// abort
		} catch (Exception e) {
			ThreadManager.ERROR = e;
			ThreadManager.MAIN.interrupt();
		}
	}

	synchronized public void setdepth() {
		depth = depthincremeneter;
		depthincremeneter += 1;
	}

	synchronized private void onend() {
		if (Thread.interrupted()) {
			return;
		}
		AiThread.FINISHED.put(depth, this);
		AiThread.STARTED.add(new AiThread(state));
	}

	public static synchronized void reset() {
		AiThread.FINISHED.clear();
		AiThread.STARTED.clear();
	}
}