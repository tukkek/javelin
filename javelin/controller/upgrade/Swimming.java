package javelin.controller.upgrade;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class Swimming extends WalkingSpeed {
	public Swimming(String name, int target) {
		super(name, target);
	}

	@Override
	public boolean apply(Combatant m) {
		/*
		 * ignoring water is only a subset of flying so there is no point in
		 * buying swim if you already fly.
		 * 
		 * Having less than double walking speed is senseless since water halves
		 * walking speed.
		 */
		return m.source.fly == 0 && target > m.source.walk / 2
				&& super.apply(m);
	}

	@Override
	protected long getSpeed(Monster m) {
		return m.swim;
	}

	@Override
	protected void setSpeed(Monster m) {
		m.swim = target;
	}
}
