package javelin.controller.upgrade;

import javelin.model.unit.Monster;

public class Flying extends WalkingSpeed {
	public Flying(String name, int target) {
		super(name, target);
	}

	@Override
	protected long getSpeed(Monster m) {
		return m.fly;
	}

	@Override
	protected void setSpeed(Monster m) {
		m.fly = target;
	}
}
