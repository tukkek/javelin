package javelin.controller.generator.dungeon.tables;

import tyrant.mikera.engine.RPG;

public class Row implements Cloneable {
	int min;
	int max;
	int change;
	boolean optional;

	public Row(int min, int max, int change, boolean optional) {
		super();
		this.min = min;
		this.max = max;
		this.change = change;
		this.optional = optional;
		change();
	}

	void change() {
		int amount = RPG.r(-change, +change);
		min += amount;
		max += amount;
		if (!optional) {
			min = Math.max(1, min);
			max = Math.max(1, max);
		}
	}

	public int getamount() {
		return RPG.r(min, max);
	}

	@Override
	protected Row clone() {
		try {
			return (Row) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}