package javelin.controller.generator.dungeon.tables;

import javelin.controller.Point;

public class RoomSizeTable extends NumericTable {
	public class RoomSize extends NumericTable {
		public RoomSize(int min, int max, int delta, boolean optional) {
			super(min, max, delta, optional);
		}

		@Override
		protected void modify(int change) {
			super.modify(change);
			while (max * max < 12) {
				super.modify();
			}
		}
	}

	NumericTable width = new RoomSize(3, 7, 3, false);
	NumericTable height = new RoomSize(3, 7, 3, false);

	public RoomSizeTable() {
		super(0, 0, 0, false);
	}

	@Override
	protected void modify() {
		width.modify();
		height.modify(width.change);
	}

	@Override
	public NumericValue roll() {
		throw new UnsupportedOperationException();
	}

	public Point rolldimensions() {
		return new Point(width.rollvalue(), height.rollvalue());
	}
}
