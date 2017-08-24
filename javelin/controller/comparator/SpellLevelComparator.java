package javelin.controller.comparator;

import java.util.Comparator;

import javelin.controller.upgrade.Spell;

public class SpellLevelComparator implements Comparator<Spell> {
	public static final SpellLevelComparator SINGLETON = new SpellLevelComparator();

	private SpellLevelComparator() {
		// use SINGLETON
	}

	@Override
	public int compare(Spell o1, Spell o2) {
		return o1.level - o2.level;
	}
}
