package javelin.model.unit.feat;

import javelin.model.unit.attack.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class Toughness extends Feat {
	/** Unique instance of this {@link Feat}. */
	public static final Toughness SINGLETON = new Toughness();
	public static final int HP = 3;

	private Toughness() {
		super("toughness");
		stack = true;
	}

	@Override
	public String inform(final Combatant m) {
		return "Max HP: " + m.maxhp;
	}

	@Override
	public boolean upgrade(final Combatant m) {
		if (!super.upgrade(m)) {
			return false;
		}
		m.maxhp += 3;
		m.hp += 3;
		m.source.hd.extrahp += HP;
		return true;
	}
}
