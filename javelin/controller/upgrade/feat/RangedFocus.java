package javelin.controller.upgrade.feat;

import java.util.List;

import javelin.model.unit.AttackSequence;
import javelin.model.unit.Monster;

public class RangedFocus extends MeleeFocus {
	public RangedFocus(final String name) {
		super(name);
	}

	@Override
	protected List<AttackSequence> getattacks(final Monster m) {
		return m.ranged;
	}
}
