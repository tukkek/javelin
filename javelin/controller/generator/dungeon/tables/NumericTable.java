package javelin.controller.generator.dungeon.tables;

import tyrant.mikera.engine.RPG;

/**
 * Subverts {@link DungeonTable} to instead provice a number and not a
 * {@link Row} object, making it easier to implement things like "room size
 * table". A value can be returned via {@link #rollvalue()}.
 *
 * Subclasses will need to implement a zero-argument constructor.
 *
 * @author alex
 */
public class NumericTable extends DungeonTable {
	int min;
	int max;
	int delta;
	boolean optional;
	/**
	 * Exposed amount of modification.
	 */
	public int change;

	class NumericValue extends Row {
		final int value;

		public NumericValue(int value) {
			super(1, 1, 0, false);
			this.value = value;
		}
	}

	public NumericTable(int min, int max, int delta, boolean optional) {
		super();
		this.min = min;
		this.max = max;
		this.delta = delta;
		this.optional = optional;
	}

	@Override
	public NumericValue roll() {
		return (NumericValue) super.roll();
	}

	public int rollvalue() {
		return roll().value;
	}

	@Override
	protected void modify() {
		change = RPG.r(-delta, +delta);
		modify(change);
	}

	void modify(int change) {
		int floor = optional ? 0 : 1;
		min = Math.max(min + change, floor);
		max = Math.max(max + change, floor);
		table.clear();
		for (int i = min; i <= max; i++) {
			add(new NumericValue(i));
		}
	}
}
