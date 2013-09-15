package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.upgrade.Spell;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.RecruitScreenOption;
import javelin.view.screen.town.option.ScreenOption;
import javelin.view.screen.town.option.ShopScreenOption;
import javelin.view.screen.town.option.UpgradingScreenOption;

public class RestingScreen extends PurchaseScreen {
	private static final Option INN = new Option("Sleep at lodge", 0);
	private Option week = new Option("Sleep at lodge (1 week)",
			7 * .5 * Squad.active.size());
	private final Option hospital = new Option("Recover in hospital",
			Squad.active.size() * .7);
	public boolean slept = false;

	public RestingScreen(final Town t) {
		super("Rest?", t);
	}

	@Override
	boolean select(final Option o) {
		if (o instanceof ScreenOption) {
			ScreenOption screen = (ScreenOption) o;
			screen.show().show();
			return true;
		}
		if (!super.select(o)) {
			return false;
		}
		int restperioeds = 1;
		int hours = 8;
		if (o == week) {
			restperioeds = 14;
			hours = 7 * 24;
		} else if (o == hospital) {
			restperioeds = 2;
		}
		for (int i = 0; i < restperioeds; i++) {
			recover();
		}
		Squad.active.hourselapsed += hours;
		slept = true;
		return true;
	}

	public static void recover() {
		for (final Combatant m : Squad.active.members) {
			int heal = m.source.hd.countdice();
			if (heal < 1) {
				heal = 1;
			}
			m.hp += heal;
			if (m.hp > m.maxhp) {
				m.hp = m.maxhp;
			}
			for (Spell p : m.spells) {
				p.used = 0;
			}
		}
	}

	@Override
	List<Option> getOptions() {
		final ArrayList<Option> list = new ArrayList<Option>();
		list.add(new RecruitScreenOption("Recruit", town));
		list.add(new ShopScreenOption("Shop", town));
		list.add(new UpgradingScreenOption("Upgrade", town));
		list.add(INN);
		list.add(week);
		if (hospital.price < 1) {
			hospital.price = 1;
		}
		list.add(hospital);
		return list;
	}

}
