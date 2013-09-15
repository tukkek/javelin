package javelin.view.screen.town;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.UpgradeOption;

public class UpgradingScreen extends SelectScreen {

	private final List<Option> currentoptions;

	public UpgradingScreen(final Town town) {
		super("Spend XP to upgrade your party", false, town);
		currentoptions = town.upgrades;
	}

	@Override
	boolean select(final Option op) {
		// currentoptions.remove(op);
		final UpgradeOption o = (UpgradeOption) op;
		final String parenttext = text;
		String s = "\n";
		int i = 1;
		final List<Combatant> eligible = new ArrayList<Combatant>();
		for (final Combatant m : Squad.active.members) {
			String name = m.toString();
			while (name.length() <= 10) {
				name += " ";
			}
			final BigDecimal cost = buystack(o, m.deepclone());
			if (cost != null && cost.compareTo(new BigDecimal(0)) > 0
					&& m.xp.compareTo(cost) >= 0) {
				eligible.add(m);
				String costinfo = "    Cost: "
						+ cost.multiply(new BigDecimal(100)).setScale(0,
								RoundingMode.HALF_UP) + "XP";
				s += "[" + i++ + "] " + name + " " + o.u.info(m) + costinfo
						+ " \n";
			}
		}
		text += s
				+ "\nWhich squad member? Press r to return to upgrade selection.";
		Combatant m = null;
		while (m == null) {
			Javelin.app.switchScreen(this);
			try {
				final Character input = IntroScreen.feedback();
				if (input == 'r') {
					// text += "\nBack.\n";
					text = parenttext;
					return false;
				}
				if (input == PROCEED) {
					return true;
				}
				m = eligible.get(Integer.parseInt(input.toString()) - 1);
			} catch (final NumberFormatException e) {
				continue;
			} catch (final IndexOutOfBoundsException e) {
				continue;
			}
		}
		buystack(o, m);
		new UpgradingScreen(town).show();
		return true;
	}

	public BigDecimal buystack(final UpgradeOption o, Combatant m) {
		Double cost = buyonce(o, m);
		if (cost == null) {
			return null;
		}
		if (true || !o.u.isstackable()) {
			return new BigDecimal(cost);
		}
		BigDecimal total = new BigDecimal(cost);
		while (m.xp.compareTo(new BigDecimal(0)) >= 0) {
			Combatant temp = m.clone();
			temp.source = temp.source.clone();
			cost = buyonce(o, temp);
			if (cost == null) {
				return total;
			}
			if (temp.xp.compareTo(new BigDecimal(0)) <= 0) {
				break;
			}
			total = total.add(new BigDecimal(cost));
			buyonce(o, m);
		}
		return total;
	}

	private Double buyonce(final UpgradeOption o, final Combatant m) {
		float originalcr = ChallengeRatingCalculator.calculaterawcr(m.source)[1];
		if (!o.u.apply(m)) {
			return null;
		}
		Double cost = new Double(
				ChallengeRatingCalculator.calculaterawcr(m.source)[1]
						- originalcr);
		m.xp = m.xp.subtract(new BigDecimal(cost));
		return cost;
	}

	@Override
	String printInfo() {
		return "";
	}

	@Override
	public String getCurrency() {
		return "XP";
	}

	@Override
	List<Option> getOptions() {
		return currentoptions;
	}

	@Override
	protected Comparator<Option> sort() {
		return new Comparator<Option>() {
			@Override
			public int compare(Option arg0, Option arg1) {
				return arg0.name.compareTo(arg1.name);
			}
		};
	}
}
