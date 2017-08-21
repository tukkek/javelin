package javelin.view.screen;

import java.util.ArrayList;
import java.util.List;

import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.labor.base.Dwelling;
import javelin.model.world.location.unique.Haxor;
import javelin.model.world.location.unique.MercenariesGuild;
import javelin.view.screen.shopping.ShoppingScreen;
import javelin.view.screen.town.SelectScreen;

public class HauntScreen extends SelectScreen {
	static final Option PILLAGE = new Option("Pillage this location for 1 ruby",
			0, 'p');

	class RecruitOption extends Option {
		Monster m;

		public RecruitOption(Monster m) {
			super("Recruit " + m.name.toLowerCase(), 0);
			this.m = m;
			addprice();
		}

		void addprice() {
			name += " (" + Math.round(m.challengerating * 100) + "XP)";
		}
	}

	class HireOption extends RecruitOption {
		public HireOption(Monster m) {
			super(m);
			name = "Hire " + m.name.toLowerCase();
			addprice();
		}

		@Override
		void addprice() {
			final String fee = ShoppingScreen
					.formatcost(MercenariesGuild.getfee(m));
			name += " ($" + fee + "/day)";
		}
	}

	Haunt haunt;
	String extrainfo = "";

	public HauntScreen(Haunt haunt) {
		super("You enter the " + haunt.descriptionknown.toLowerCase() + ".",
				null);
		this.haunt = haunt;
	}

	@Override
	public String printinfo() {
		String info;
		if (haunt.available.isEmpty()) {
			info = "This location is empty right now. You should come back later.";
		} else {
			info = "You have $" + ShoppingScreen.formatcost(Squad.active.gold)
					+ " and " + Squad.active.sumxp() + "XP";
		}
		if (!extrainfo.isEmpty()) {
			info += "\n\n" + extrainfo;
		}
		return info;
	}

	@Override
	public List<Option> getoptions() {
		ArrayList<Option> options = new ArrayList<Option>();
		for (Monster m : haunt.available) {
			options.add(new HireOption(m));
			options.add(new RecruitOption(m));
		}
		options.add(PILLAGE);
		return options;
	}

	@Override
	public boolean select(Option o) {
		if (o == PILLAGE) {
			Haxor.singleton.rubies += 1;
			haunt.remove();
			return true;
		}
		Class<? extends Option> optiontype = o.getClass();
		Monster m = ((RecruitOption) o).m;
		if (optiontype.equals(HireOption.class)) {
			if (MercenariesGuild.recruit(new Combatant(m.clone(), true),
					false)) {
				haunt.available.remove(m);
			} else {
				extrainfo = "You don't have enough gold...";
			}
			return true;
		}
		if (optiontype.equals(RecruitOption.class)) {
			if (Dwelling.recruit(m)) {
				haunt.available.remove(m);
			} else {
				extrainfo = "You don't have enough XP...";
			}
			return true;
		}
		return false;
	}

	@Override
	public String getCurrency() {
		return null; // see #printpriceinfo
	}

	@Override
	public String printpriceinfo(Option o) {
		return "";
	}
}
