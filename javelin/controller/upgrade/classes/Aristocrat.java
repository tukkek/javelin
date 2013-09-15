package javelin.controller.upgrade.classes;

import javelin.model.unit.Monster;

public class Aristocrat extends ClassAdvancement {
	public Aristocrat(String name) {
		super(name);
	}

	@Override
	protected int gethd() {
		return 8;
	}

	@Override
	protected void setlevel(int level, Monster m) {
		m.aristocrat = level;
	}

	@Override
	public int getlevel(Monster m) {
		return m.aristocrat;
	}

	@Override
	public Level[] gettable() {
		return new Level[] { new Level(0, 0, 0, 0), new Level(0, 0, 0, 2),
				new Level(1, 0, 0, 3), new Level(2, 1, 1, 3),
				new Level(3, 1, 1, 4), new Level(3, 1, 1, 4),
				new Level(4, 2, 2, 5), new Level(5, 2, 2, 5),
				new Level(6, 2, 2, 6), new Level(6, 3, 3, 6),
				new Level(7, 3, 3, 7), new Level(8, 3, 3, 7),
				new Level(9, 4, 4, 8), new Level(9, 4, 4, 8),
				new Level(10, 4, 4, 9), new Level(11, 5, 5, 9),
				new Level(12, 5, 5, 10), new Level(12, 5, 5, 10),
				new Level(13, 6, 6, 10), new Level(14, 6, 6, 11),
				new Level(15, 6, 6, 12), };
	}

}
