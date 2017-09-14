package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.feat.Feat;

public class FeatByNameComparator implements Comparator<Feat> {
	public static final FeatByNameComparator INSTANCE = new FeatByNameComparator();

	private FeatByNameComparator() {
		// prevent instantiation
	}

	@Override
	public int compare(Feat o1, Feat o2) {
		return o1.name.compareTo(o2.name);
	}
}