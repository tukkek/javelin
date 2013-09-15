package javelin.controller.upgrade.classes;

import javelin.model.unit.Monster;

public class Warrior extends ClassAdvancement {
	public Warrior(String name) {
		super(name);
	}

	@Override
	protected int gethd() {
		return 8;
	}

	@Override
	protected void setlevel(int level, Monster m) {
		m.warrior = level;
	}

	@Override
	public int getlevel(Monster m) {
		return m.warrior;
	}

	@Override
	public Level[] gettable() {
		return new Level[] { new Level(0, 0, 0, 0), new Level(1, 2, 0, 0),
				new Level(2, 3, 0, 0), new Level(3, 3, 1, 1),
				new Level(4, 4, 1, 1), new Level(5, 4, 1, 1),
				new Level(6, 5, 2, 2), new Level(7, 5, 2, 2),
				new Level(8, 6, 2, 2), new Level(9, 6, 3, 3),
				new Level(10, 7, 3, 3), new Level(11, 7, 3, 3),
				new Level(12, 8, 4, 4), new Level(13, 8, 4, 4),
				new Level(14, 9, 4, 4), new Level(15, 9, 5, 5),
				new Level(16, 10, 5, 5), new Level(17, 10, 5, 5),
				new Level(18, 11, 6, 6), new Level(19, 11, 6, 6),
				new Level(20, 12, 6, 6), };
	}

}
