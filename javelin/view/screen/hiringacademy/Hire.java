package javelin.view.screen.hiringacademy;

import javelin.model.unit.attack.Combatant;
import javelin.view.screen.Option;

class Hire extends Option {
	Combatant c;

	public Hire(Combatant c, int xpcost) {
		super("Recruit: " + c.toString().toLowerCase() + " (" + xpcost + "XP)",
				0);
		this.c = c;
		price = xpcost;
	}

	public Hire(Combatant c) {
		this(c, Math.round(c.source.challengerating * 100));
	}
}