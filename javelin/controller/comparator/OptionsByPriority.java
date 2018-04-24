package javelin.controller.comparator;

import java.util.Comparator;

import javelin.view.screen.Option;

public class OptionsByPriority implements Comparator<Option> {
	public static final OptionsByPriority INSTANCE = new OptionsByPriority();

	private OptionsByPriority() {
		// singleton
	}

	@Override
	public int compare(Option a, Option b) {
		return a.priority == b.priority ? Double.compare(a.sort(), b.sort())
				: Float.compare(a.priority, b.priority);
	}
}
