package javelin.model.feat.attack.focus;

import java.util.List;

import javelin.model.feat.Feat;
import javelin.model.unit.AttackSequence;
import javelin.model.unit.Monster;

/**
 * @see WeaponFocus
 * @author alex
 */
public class RangedFocus extends WeaponFocus {
	/** Unique instance of this {@link Feat}. */
	public static final RangedFocus SINGLETON = new RangedFocus();

	/** Constructor. */
	private RangedFocus() {
		super("Ranged focus");
	}

	@Override
	protected List<AttackSequence> getattacks(final Monster m) {
		return m.ranged;
	}
}
