package javelin.model.item.scroll;

import javelin.model.item.Item;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class RessurectScroll extends RaiseScroll {

	public RessurectScroll() {
		super("Scroll of ressurect & restoration", 13000, Item.GOOD);
	}

	@Override
	protected void givelife(Combatant m) {
		m.hp = m.maxhp;
	}
}
