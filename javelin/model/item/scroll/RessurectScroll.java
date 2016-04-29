package javelin.model.item.scroll;

import javelin.controller.challenge.factor.SpellsFactor;
import javelin.model.item.Item;
import javelin.model.unit.Combatant;

/**
 * See the d20 SRD for more info.
 */
public class RessurectScroll extends RaiseScroll {

	public RessurectScroll() {
		super("Scroll of ressurection & restoration", 13000, Item.GOOD, 7,
				SpellsFactor.ratespelllikeability(7)
						+ RaiseScroll.RESTORATIONCOST);
	}

	@Override
	protected void givelife(Combatant m) {
		m.hp = m.maxhp;
	}
}
