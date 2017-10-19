package javelin.view.screen.upgrading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.CrCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.ClassLevelUpgrade;
import javelin.model.unit.Squad;
import javelin.model.unit.abilities.spell.Spell;
import javelin.model.unit.attack.Combatant;
import javelin.model.unit.feat.Feat;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;

/**
 * Lets a plear {@link Upgrade} members from a {@link Squad}.
 *
 * Upgrading 1 level (100XP) should take 1 week and cost $50.
 *
 * @author alex
 */
public abstract class UpgradingScreen extends SelectScreen {
	public class UpgradeOption extends Option {
		/** Upgrade in question. */
		public final Upgrade u;

		/** Constructor. */
		public UpgradeOption(final Upgrade u) {
			super(u.name, 0);
			this.u = u;
			if (u instanceof Spell) {
				name = "Spell: " + name.toLowerCase();
			}
		}
	}

	final HashMap<Integer, Combatant> original = new HashMap<Integer, Combatant>();
	final HashSet<Combatant> upgraded = new HashSet<Combatant>();
	protected boolean showmoneyinfo = true;

	/**
	 * Constructor.
	 *
	 * @param t
	 *            Can be <code>null</code>, only {@link TownUpgradingScreen}
	 *            depends on it.
	 */
	public UpgradingScreen(String name, Town t) {
		super(name, t);
		for (Combatant c : Squad.active.members) {
			original.put(c.id, c.clone().clonesource());
		}
	}

	/**
	 * @param trainee
	 *            Unit that has been taken out of it's {@link Squad} for
	 *            training.
	 */
	protected abstract void registertrainee(Order trainee);

	/**
	 * Mostly concerned with {@link Squad} clean-up issues.
	 * 
	 * @param trainees
	 */
	protected abstract void onexit(Squad s, ArrayList<TrainingOrder> trainees);

	/** Available {@link UpgradeOption}s. */
	protected abstract Collection<Upgrade> getupgrades();

	@Override
	public boolean select(final Option op) {
		final UpgradeOption o = (UpgradeOption) op;
		final String parenttext = text;
		final List<Combatant> eligible = new ArrayList<Combatant>();
		String listeligible = listeligible(o, eligible);
		if (eligible.isEmpty()) {
			print(text + "\nNone can learn this right now...\n");
			return false;
		}
		text += listeligible;
		if (showmoneyinfo) {
			text += "Your squad has $" + Squad.active.gold + "\n\n";
		}
		text += "Which squad member? Press r to return to upgrade selection.";
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

	void finishpurchase(final UpgradeOption o, Combatant c) {
		if (buy(o, c, false) != null) {
			update(c);
			upgraded.add(c);
			c.postupgrade(o.u instanceof ClassLevelUpgrade
					? (ClassLevelUpgrade) o.u : null);
		}
	}

	void update(Combatant c) {
		for (Feat f : c.source.feats) {
			f.update(c);
		}
	}

	String listeligible(final UpgradeOption o, final List<Combatant> eligible) {
		String s = "\n";
		int i = 1;
		for (final Combatant c : Squad.active.members) {
			String name = c.toString();
			while (name.length() <= 10) {
				name += " ";
			}
			final BigDecimal cost = buy(o, c.clone().clonesource(), true);
			if (cost != null && cost.compareTo(new BigDecimal(0)) > 0
					&& c.xp.compareTo(cost) >= 0) {
				eligible.add(c);
				String costinfo = "    Cost: "
						+ cost.multiply(new BigDecimal(100)).setScale(0,
								RoundingMode.HALF_UP)
						+ "XP, $" + price(cost.floatValue());
				s += "[" + i++ + "] " + name + " " + o.u.inform(c) + costinfo
						+ ", "
						+ Math.round(
								cost.floatValue() * TrainingOrder.UPGRADETIME)
						+ " days\n";
			}
		}
		return s + "\n";
	}

	private int price(float xp) {
		return Math.round(xp * 50);
	}

	private BigDecimal buy(final UpgradeOption o, Combatant c,
			boolean listing) {
		float originalcr = CrCalculator.calculaterawcr(c.source)[1];
		final Combatant clone = c.clone().clonesource();
		if (!upgrade(o, clone)) {
			return null;
		}
		BigDecimal cost = new BigDecimal(
				CrCalculator.calculaterawcr(clone.source)[1] - originalcr);
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
		upgrade(o, c);
		c.xp = c.xp.subtract(cost);
		return cost;
	}

	protected boolean upgrade(final UpgradeOption o, final Combatant c) {
		return o.u.upgrade(c);
	}

	@Override
	public String printinfo() {
		return "";
	}

	@Override
	public String getCurrency() {
		return "XP";
	}

	@Override
	public List<Option> getoptions() {
		Collection<Upgrade> upgrades = getupgrades();
		ArrayList<Option> ups = new ArrayList<Option>(upgrades.size());
		for (Upgrade u : upgrades) {
			ups.add(createoption(u));
		}
		return ups;
	}

	protected UpgradeOption createoption(Upgrade u) {
		return new UpgradeOption(u);
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
		ArrayList<TrainingOrder> trainees = new ArrayList<TrainingOrder>();
		Squad s = Squad.active;
		for (Combatant c : upgraded) {
			Combatant original = this.original.get(c.id);
			float xpcost = CrCalculator.calculaterawcr(c.source)[1]
					- CrCalculator.calculaterawcr(original.source)[1];
			trainees.add(new TrainingOrder(c, s.equipment.get(c.id),
					c.toString(), xpcost, original));
		}
		onexit(s, trainees);
		for (TrainingOrder trainee : trainees) {
			registertrainee(trainee);
			Combatant c = trainee.trained;
			s.equipment.remove(c.toString());
			s.remove(c);
		}
	}

}