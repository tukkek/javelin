package javelin.model.item;

import javelin.model.unit.Combatant;

public class RessurectScroll extends RaiseScroll {

	public RessurectScroll() {
		super("Scroll of ressurect & restoration", 13000,
				"After battle, ressurects a killed ally with full hit points");
	}

	@Override
	protected void givelife(Combatant m) {
		m.hp = m.maxhp;
	}
}
