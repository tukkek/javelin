package javelin.controller.upgrade;

import javelin.model.unit.Combatant;

public class Vision extends Upgrade {

	final private int target;

	public Vision(String name, int targetp) {
		super(name);
		target = targetp;
	}

	@Override
	public String info(Combatant m) {
		switch (m.source.vision) {
		case 0:
			return "Currently: mormal vision";
		case 1:
			return "Currently: low-light vision";
		case 2:
			return "Currently: darkvision";
		}
		throw new RuntimeException("Unknown vision");
	}

	@Override
	public boolean apply(Combatant m) {
		if (m.source.vision >= target) {
			return false;
		}
		m.source.vision = target;
		return true;
	}

	@Override
	public boolean isstackable() {
		return false;
	}

}
