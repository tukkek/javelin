package javelin.controller.quality;

import javelin.model.unit.Monster;

/**
 * @see Monster#vision
 * 
 * @author alex
 */
public class Vision extends Quality {
	final int target;

	public Vision(String name, int target) {
		super(name);
		this.target = target;
	}

	@Override
	public void add(final String declaration, final Monster m) {
		if (target > m.vision) {
			m.vision = target;
		}
	}

	@Override
	public boolean has(Monster monster) {
		return monster.vision > 0;
	}

	@Override
	public float rate(Monster monster) {
		if (monster.vision == 2) {
			return .2f;
		}
		if (monster.vision == 1) {
			return .1f;
		}
		return 0;
	}
}