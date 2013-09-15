package javelin.controller.quality;

import javelin.model.unit.Monster;

public class SpecialPerception extends Quality {
	final int target;

	public SpecialPerception(String name, int target) {
		super(name);
		this.target = target;
	}

	@Override
	public void add(final String declaration, final Monster m) {
		if (target > m.vision) {
			m.vision = target;
		}
	}
}