package javelin.controller.ai.cache;

import javelin.Javelin;

/**
 * Efficient thread to clear up a {@link Cache}.
 * 
 * @author alex
 */
public class CacheClearThread extends Thread {

	private static final int MAXDEPTH = 15;
	final private Link root;
	private int start;
	final private int end;

	public CacheClearThread(final Link root, final int start, final int end) {
		this.root = root;
		this.start = start;
		this.end = end;
		start();
	}

	@Override
	public void run() {
		for (; start <= end; start++) {
			final Link l = root.cache[start];
			if (l == null) {
				return;
			}
			l.payload = null;
			final Link[] stack = new Link[MAXDEPTH];
			final int[] index = new int[MAXDEPTH];
			stack[0] = l;
			index[0] = 0;
			int depth = 0;
			while (depth < stack.length && depth < index.length) {
				final Link step = stack[depth].cache[index[depth]];
				stack[depth].payload = null;
				if (index[depth] == Cache.CACHESIZE || step == null) {
					depth -= 1;
					if (--depth < 0) {
						break;
					}
					index[depth] += 1;
					continue;
				}
				step.payload = null;
				if (step.cache[0] == null) {
					index[depth] += 1;
				} else {
					depth += 1;
					if (depth >= stack.length && depth >= index.length) {
						if (Javelin.DEBUG) {
							System.out.println("cache clear issue");
							// TODO debug
						}
						break;
					}
					stack[depth] = step;
					index[depth] = 0;
				}
			}
			if (Javelin.DEBUG && ++check == 100) {
				check = 0;
				checkclean(l);
			}
		}
	}

	static int check = 0;

	static private void checkclean(Link l) {
		if (l.payload != null) {
			assert false;
		}
		Link[] cache = l.cache;
		for (int i = 0; i < cache.length; i++) {
			Link m = cache[i];
			if (m != null) {
				checkclean(m);
			}
		}
	}
}
