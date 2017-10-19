package javelin.model.unit.feat;

import javelin.model.unit.attack.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class ImprovedInitiative extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final ImprovedInitiative SINGLETON = new ImprovedInitiative();

	private ImprovedInitiative() {
		super("improved initiative");
	}

	@Override
	public String inform(final Combatant m) {
		return "Current initiative: " + m.source.initiative;
	}

	@Override
	public boolean upgrade(final Combatant m) {
		if (super.upgrade(m)) {
			m.source.initiative += 4;
			return true;
		}
		return false;
	}
}
