package javelin.controller.upgrade.feat;

import javelin.model.unit.Combatant;

/**
 * @see javelin.model.feat.Toughness
 * @author alex
 */
public class Toughness extends FeatUpgrade {
	public Toughness() {
		super("Toughness", javelin.model.feat.Toughness.singleton);
		stack = true;
	}

	@Override
	public String info(final Combatant m) {
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