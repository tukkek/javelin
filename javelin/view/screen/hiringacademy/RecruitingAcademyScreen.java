package javelin.view.screen.hiringacademy;

import java.util.List;

import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.base.Dwelling;
import javelin.model.world.location.town.labor.military.Academy;
import javelin.view.screen.Option;
import javelin.view.screen.upgrading.AcademyScreen;

public class RecruitingAcademyScreen extends AcademyScreen {
	HiringAcademy hiringacademy;

	public RecruitingAcademyScreen(Academy academy, Town t) {
		super(academy, t);
		hiringacademy = (HiringAcademy) academy;
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
		return Dwelling.sumxp() > h.price;
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
		return "You currently have " + Dwelling.sumxp() + "XP";
	}
}