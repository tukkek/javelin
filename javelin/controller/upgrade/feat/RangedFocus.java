package javelin.controller.upgrade.feat;

import java.util.List;

import javelin.model.feat.WeaponFocus;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Monster;

/**
 * @see WeaponFocus
 * @author alex
 */
public class RangedFocus extends MeleeFocus {
	/** Constructor. */
	public RangedFocus(final String name) {
		super(name);
	}

	@Override
	protected List<AttackSequence> getattacks(final Monster m) {
		return m.ranged;
	}
}
