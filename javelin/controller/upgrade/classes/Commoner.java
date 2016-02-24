package javelin.controller.upgrade.classes;

import javelin.model.unit.Monster;

/**
 * @see ClassAdvancement
 */
public class Commoner extends ClassAdvancement {
	public Commoner(String name) {
		super(name);
	}

	@Override
	public Level[] gettable() {
		return new Level[] { new Level(0, 0, 0, 0), new Level(0, 0, 0, 0),
				new Level(1, 0, 0, 0), new Level(1, 1, 1, 1),
				new Level(2, 1, 1, 1), new Level(2, 1, 1, 1),
				new Level(3, 2, 2, 2), new Level(3, 2, 2, 2),
				new Level(4, 2, 2, 2), new Level(4, 3, 3, 3),
				new Level(5, 3, 3, 3), new Level(5, 3, 3, 3),
				new Level(6, 4, 4, 4), new Level(6, 4, 4, 4),
				new Level(7, 4, 4, 4), new Level(7, 5, 5, 5),
				new Level(8, 5, 5, 5), new Level(8, 5, 5, 5),
				new Level(9, 6, 6, 6), new Level(9, 6, 6, 6),
				new Level(10, 6, 6, 6), };
	}

	@Override
	protected int gethd() {
		return 4;
	}

	@Override
	protected void setlevel(int level, Monster m) {
		m.commoner = level;
	}

	@Override
	public int getlevel(Monster m) {
		return m.commoner;
	}

}
