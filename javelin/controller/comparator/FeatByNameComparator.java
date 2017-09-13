package javelin.controller.comparator;

import java.util.Comparator;

import javelin.model.unit.feat.Feat;

public class FeatByNameComparator implements Comparator<Feat> {
	@Override
	public int compare(Feat o1, Feat o2) {
		return o1.name.compareTo(o2.name);
	}
}