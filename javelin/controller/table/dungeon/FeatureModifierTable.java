package javelin.controller.table.dungeon;

import javelin.controller.table.Table;
import javelin.old.RPG;

public class FeatureModifierTable extends Table {
	enum Modifier {
		VERYEASY, EASY, NORMAL, HARD, VERYHARD,
	}

	public FeatureModifierTable() {
		add(Modifier.VERYEASY, 2);
		add(Modifier.EASY, 2);
		add(Modifier.NORMAL, 4);
		add(Modifier.HARD, 2);
		add(Modifier.VERYHARD, 1);
	}

	public int rollmodifier() {
		Object modifier = roll();
		if (modifier == Modifier.VERYEASY) {
			return -RPG.r(1, 8);
		}
		if (modifier == Modifier.EASY) {
			return -RPG.r(1, 4);
		}
		if (modifier == Modifier.NORMAL) {
			return -0;
		}
		if (modifier == Modifier.HARD) {
			return +RPG.r(1, 4);
		}
		return +RPG.r(1, 8);
	}
}
