package javelin.view.screen.hiringacademy;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.town.PurchaseScreen;

public class HiringAcademyScreen extends RecruitingAcademyScreen {

	public HiringAcademyScreen(Academy academy) {
		super(academy, null);
	}

	@Override
	Hire createoption(Combatant c) {
		Hire h = super.createoption(c);
		h.price = MercenariesGuild.getfee(c);
		h.name = "Hire: " + c.toString().toLowerCase() + " ($"
				+ PurchaseScreen.formatcost(h.price) + "/day)";
		return h;
	}

	@Override
	boolean canafford(Hire h) {
		return Squad.active.gold >= h.price;
	}

	@Override
	void spend(Hire h) {
		Squad.active.gold -= h.price;
		h.c.mercenary = true;
	}

	@Override
	String printresourcesinfo() {
		return "You currently have $"
				+ PurchaseScreen.formatcost(Squad.active.gold);
	}
}
