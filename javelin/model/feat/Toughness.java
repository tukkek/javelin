package javelin.model.feat;

import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Toughness extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final Toughness SINGLETON = new Toughness();

	private Toughness() {
		super("toughness");
		stack = true;
	}

	@Override
	public String inform(final Combatant m) {
		return "Max HP: " + m.maxhp;
	}

	@Override
	public boolean apply(final Combatant m) {
		if (!super.apply(m)) {
			return false;
		}
		m.maxhp += 3;
		m.hp += 3;
		m.source.hd.extrahp += 3;
		return true;
	}
}
