package javelin.controller.comparator;

import java.util.Comparator;
import java.util.List;

public class SizeComparator implements Comparator<List<?>> {
	static public final SizeComparator INSTANCE = new SizeComparator();

	private SizeComparator() {
		// singleton
	}

	@Override
	public int compare(List<?> o1, List<?> o2) {
		return Integer.compare(o1.size(), o2.size());
	}
}
