package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.Monster;

public class MonsterNameComparator implements Comparator<Monster> {
	public static final Comparator<? super Monster> INSTANCE = new MonsterNameComparator();

	private MonsterNameComparator() {
		// prevent instantiation
	}

	@Override
	public int compare(Monster o1, Monster o2) {
		return o1.name.compareTo(o2.name);
	}
}
