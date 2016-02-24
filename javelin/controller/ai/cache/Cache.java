package javelin.controller.ai.cache;

import java.util.List;

import javelin.controller.ai.AiThread;
import javelin.controller.ai.ThreadManager;

/**
 * Efficient data-structe based on an array of {@link Link}.
 * 
 * @author alex
 */
public class Cache {
	static final int CACHESIZE = 30;

	/**
	 * Needed to help prevent calculating nodes more than once - should always
	 * be faster just to wait.
	 */
	boolean updating = false;
	Link root = new Link();

	public Object get(List<Integer> index) {
		AiThread.checkinterrupted();
		if (updating) {
			unlock();
			// ValueSelector.checkinterrupted();
		}
		Link l = root;
		for (int partial : index) {
			l = l.cache[partial];
			if (l == null) {
				return null;
			}
		}
		return l.payload;
	}

	synchronized void unlock() {
		// wait for update to finish
	}

	synchronized public void put(List<Integer> index, Object o) {
		updating = true;
		Link l = root;
		for (int partial : index) {
			if (l.cache[partial] == null) {
				Link created = new Link();
				l.cache[partial] = created;
				l = created;
			} else {
				l = l.cache[partial];
			}
		}
		l.payload = o;
		updating = false;
	}

	public void clear() {
		root.payload = null;
		int used = 1;
		while (root.cache[used] != null) {
			used += 1;
		}
		final int perthread = (int) Math
				.round(Math.ceil(used / (float) ThreadManager.maxthreads));
		final Thread[] pool = new Thread[ThreadManager.maxthreads];
		for (int i = 0; i < ThreadManager.maxthreads; i++) {
			final int start = i * perthread;
			final int end = start + perthread - 1;
			pool[i] =
					new CacheClearThread(root, start, end > used ? used : end);
			if (end >= used) {
				break;
			}
		}
		for (final Thread t : pool) {
			if (t == null) {
				return;
			}
			try {
				t.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	static int _largest;

	/**
	 * Useful to determine {@link #CACHESIZE}.
	 */
	public void _findlargest() {
		Cache._largest = 0;
		Cache._findlargest(root);
		System.out.println("Largest: " + Cache._largest);
	}

	static void _findlargest(Link l) {
		for (int i = 0; i < l.cache.length; i += 1) {
			Link nested = l.cache[i];
			if (nested != null) {
				if (nested.payload != null && i > Cache._largest) {
					Cache._largest = i;
				}
				Cache._findlargest(nested);
			}
		}
	}
}
