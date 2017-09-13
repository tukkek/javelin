package javelin.view.screen.hiringacademy;

import java.util.List;

import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.fortification.Guild;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.view.screen.Option;
import javelin.view.screen.upgrading.AcademyScreen;

public class RecruitingGuildScreen extends AcademyScreen {
	Guild hiringacademy;

	public RecruitingGuildScreen(Guild academy) {
		super(academy, null);
		hiringacademy = academy;
		this.showmoneyinfo = false;
	}

	@Override
	public List<Option> getoptions() {
		List<Option> options = super.getoptions();
		super.sort(options);
		for (Combatant c : hiringacademy.gethires()) {
			if (c != null) {
				options.add(createoption(c));
			}
		}
		return options;
	}

	Hire createoption(Combatant c) {
		return new Hire(c);
	}

	@Override
	protected void sort(List<Option> options) {
		// see #getoptions
	}

	@Override
	public boolean select(Option op) {
		if (op instanceof Hire) {
			Hire h = (Hire) op;
			if (canafford(h)) {
				spend(h);
				hiringacademy.clearhire(h.c);
				Squad.active.members.add(h.c);
				return true;
			}
			print(text + "\nCan't afford it...");
			return false;
		}
		return super.select(op);
	}

	void spend(Hire h) {
		Dwelling.spend(h.price / 100f);
	}

	boolean canafford(Hire h) {
		return Squad.active.sumxp() > h.price;
	}

	@Override
	public String printinfo() {
		String info = super.printinfo();
		if (!info.isEmpty()) {
			info = "\n" + info;
		}
		return printresourcesinfo() + info;
	}

	String printresourcesinfo() {
		return "You currently have " + Squad.active.sumxp() + "XP";
	}
}