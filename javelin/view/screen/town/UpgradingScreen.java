package javelin.view.screen.town;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;
import javelin.model.world.Squad;
import javelin.model.world.town.Order;
import javelin.model.world.town.Town;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.UpgradeOption;
import tyrant.mikera.tyrant.InfoScreen;

/**
 * Lets a plear {@link Upgrade} members from a {@link Squad}.
 * 
 * Upgrading 1 level (100XP) should take 1 week and cost $50.
 */
public class UpgradingScreen extends SelectScreen {

	final HashMap<Integer, Combatant> original =
			new HashMap<Integer, Combatant>();
	final List<Option> currentoptions;
	final HashSet<Combatant> upgraded = new HashSet<Combatant>();

	public UpgradingScreen(final Town town) {
		super("Upgrade:", town);
		currentoptions = town.upgrades;
		for (Combatant c : Squad.active.members) {
			original.put(c.id, c.clonedeeply());
		}
	}

	@Override
	public boolean select(final Option op) {
		final UpgradeOption o = (UpgradeOption) op;
		final String parenttext = text;
		final List<Combatant> eligible = new ArrayList<Combatant>();
		text += listeligible(o, eligible) + "\nYour squad has $"
				+ Squad.active.gold
				+ "\n\nWhich squad member? Press r to return to upgrade selection.";
		Combatant c = null;
		while (c == null) {
			Javelin.app.switchScreen(this);
			try {
				final Character input = InfoScreen.feedback();
				if (input == 'r') {
					text = parenttext;
					return false;
				}
				if (input == PROCEED) {
					return true;
				}
				c = eligible.get(Integer.parseInt(input.toString()) - 1);
			} catch (final NumberFormatException e) {
				continue;
			} catch (final IndexOutOfBoundsException e) {
				continue;
			}
		}
		finishpurchase(o, c);
		return true;
	}

	public void finishpurchase(final UpgradeOption o, Combatant c) {
		if (buy(o, c, false) != null) {
			update(c);
			upgraded.add(c);
		}
	}

	private void update(Combatant c) {
		for (Feat f : c.source.feats) {
			if (f.update) {
				c.source = c.source.clone();
				f.remove(c);
				f.add(c);
			}
		}
	}

	public String listeligible(final UpgradeOption o,
			final List<Combatant> eligible) {
		String s = "\n";
		int i = 1;
		for (final Combatant m : Squad.active.members) {
			String name = m.toString();
			while (name.length() <= 10) {
				name += " ";
			}
			final BigDecimal cost = buy(o, m.clonedeeply(), true);
			if (cost != null && cost.compareTo(new BigDecimal(0)) > 0
					&& m.xp.compareTo(cost) >= 0) {
				eligible.add(m);
				String costinfo = "    Cost: "
						+ cost.multiply(new BigDecimal(100)).setScale(0,
								RoundingMode.HALF_UP)
						+ "XP, $" + price(cost.floatValue());
				s += "[" + i++ + "] " + name + " " + o.u.info(m) + costinfo
						+ ", " + Math.round(cost.floatValue() * 7) + " days\n";
			}
		}
		return s;
	}

	private int price(float xp) {
		return Math.round(xp * 50);
	}

	private BigDecimal buy(final UpgradeOption o, final Combatant c,
			boolean listing) {
		float originalcr =
				ChallengeRatingCalculator.calculaterawcr(c.source)[1];
		Combatant clone = c.clonedeeply();
		if (!o.u.apply(clone)) {
			return null;
		}
		Double cost = new Double(
				ChallengeRatingCalculator.calculaterawcr(clone.source)[1]
						- originalcr);
		if (!listing) {
			int goldpieces = price(cost.floatValue());
			if (goldpieces > Squad.active.gold) {
				text += "\n\nNot enough gold! Press any key to continue...";
				Javelin.app.switchScreen(this);
				InfoScreen.feedback();
				return null;
			}
			Squad.active.gold -= goldpieces;
		}
		o.u.apply(c);
		c.xp = c.xp.subtract(new BigDecimal(cost));
		return new BigDecimal(cost);
	}

	@Override
	public String printInfo() {
		return "";
	}

	@Override
	public String getCurrency() {
		return "XP";
	}

	@Override
	public List<Option> getOptions() {
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

	@Override
	public String printpriceinfo(Option o) {
		return "";
	}

	@Override
	public void onexit() {
		ArrayList<Order> trainees = new ArrayList<Order>();
		Squad s = Squad.active;
		for (Combatant c : upgraded) {
			Combatant original = this.original.get(c.id);
			trainees.add(new Order(
					Math.round((ChallengeRatingCalculator
							.calculaterawcr(c.source)[1]
							- ChallengeRatingCalculator
									.calculaterawcr(original.source)[1])
							* 24 * 7 + Squad.active.hourselapsed),
					new Serializable[] { c, s.equipment.get(c.id), original }));
		}
		for (Order trainee : trainees) {
			town.training.add(trainee);
			Combatant c = (Combatant) trainee.payload[2];
			s.equipment.remove(c.toString());
			s.remove(c);
		}
		Collections.sort(town.training.queue, new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2) {
				return Math.round(o1.completionat - o2.completionat);
			}
		});
		if (Squad.squads.isEmpty()) {
			town.stash = s.gold;
		}
	}
}
