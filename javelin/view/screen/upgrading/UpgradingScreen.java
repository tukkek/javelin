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
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.feat.Feat;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.WorldActor;
import javelin.model.world.location.order.Order;
import javelin.model.world.location.order.TrainingOrder;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.SelectScreen;
import javelin.view.screen.town.option.UpgradeOption;
import tyrant.mikera.engine.RPG;

/**
 * Lets a plear {@link Upgrade} members from a {@link Squad}.
 * 
 * Upgrading 1 level (100XP) should take 1 week and cost $50.
 * 
 * @author alex
 */
public abstract class UpgradingScreen extends SelectScreen {

	final HashMap<Integer, Combatant> original =
			new HashMap<Integer, Combatant>();
	final HashSet<Combatant> upgraded = new HashSet<Combatant>();

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

	/** Mostly concerned with {@link Squad} clean-up issues. */
	protected abstract void onexit(Squad s);

	/** Available {@link UpgradeOption}s. */
	protected abstract Collection<Upgrade> getupgrades();

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

	void finishpurchase(final UpgradeOption o, Combatant c) {
		if (buy(o, c, false) != null) {
			update(c);
			upgraded.add(c);
			if (o.u.purchaseskills) {
				c.source.purchaseskills(o.u).show();
			}
		}
	}

	void update(Combatant c) {
		for (Feat f : c.source.feats) {
			if (f.update) {
				c.source = c.source.clone();
				f.remove(c);
				f.add(c);
			}
		}
	}

	String listeligible(final UpgradeOption o, final List<Combatant> eligible) {
		String s = "\n";
		int i = 1;
		for (final Combatant m : Squad.active.members) {
			String name = m.toString();
			while (name.length() <= 10) {
				name += " ";
			}
			final BigDecimal cost = buy(o, m.clone().clonesource(), true);
			if (cost != null && cost.compareTo(new BigDecimal(0)) > 0
					&& m.xp.compareTo(cost) >= 0) {
				eligible.add(m);
				String costinfo = "    Cost: "
						+ cost.multiply(new BigDecimal(100)).setScale(0,
								RoundingMode.HALF_UP)
						+ "XP, $" + price(cost.floatValue());
				s += "[" + i++ + "] " + name + " " + o.u.inform(m) + costinfo
						+ ", " + Math.round(cost.floatValue() * 7) + " days\n";
			}
		}
		return s;
	}

	private int price(float xp) {
		return Math.round(xp * 50);
	}

	private BigDecimal buy(final UpgradeOption o, Combatant c,
			boolean listing) {
		float originalcr =
				ChallengeRatingCalculator.calculaterawcr(c.source)[1];
		final Combatant clone = c.clone().clonesource();
		if (!o.u.upgrade(clone)) {
			return null;
		}
		BigDecimal cost = new BigDecimal(
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
		c.xp = c.xp.subtract(cost);
		o.u.upgrade(c);
		return cost;
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
			ups.add(new UpgradeOption(u));
		}
		return ups;
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
			float xpcost = ChallengeRatingCalculator.calculaterawcr(c.source)[1]
					- ChallengeRatingCalculator
							.calculaterawcr(original.source)[1];
			trainees.add(new TrainingOrder(Math.round(xpcost * 24 * 7), c,
					s.equipment.get(c.id), c.toString(), xpcost, original));
		}
		for (TrainingOrder trainee : trainees) {
			registertrainee(trainee);
			Combatant c = trainee.trained;
			s.equipment.remove(c.toString());
			s.remove(c);
		}
		onexit(s);
	}

	/**
	 * Applies the upgrade and adjustments.
	 * 
	 * @param memberp
	 *            Training information.
	 * @param member
	 *            Joins a nearby {@link Squad} or becomes a new one.
	 * @param p
	 *            Place the training was realized.
	 * @param member
	 *            Member to be returned (upgraded or not, in case of cancel).
	 * @return The Squad the trainee is now into.
	 */
	static public Squad completetraining(TrainingOrder memberp, WorldActor p,
			Combatant member) {
		ArrayList<Point> empty = new ArrayList<Point>();
		for (int deltax = -1; deltax <= +1; deltax++) {
			for (int deltay = -1; deltay <= +1; deltay++) {
				if (deltax == 0 && deltay == 0) {
					continue;
				}
				int x = p.x + deltax;
				int y = p.y + deltay;
				if (!World.validatecoordinate(x, y)
						|| Terrain.get(x, y).equals(Terrain.WATER)) {
					continue;
				}
				WorldActor stationed = WorldActor.get(x, y, Squad.class);
				if (stationed != null) {
					Squad.active.add(member, memberp.equipment);
					return Squad.active;
				} else if (WorldActor.get(x, y) == null) {
					empty.add(new Point(x, y));
				}
			}
		}
		Point destination = RPG.pick(empty);
		Squad s = new Squad(destination.x, destination.y,
				Math.round(Math.ceil(memberp.completionat / 24f) * 24), null);
		s.add(member, memberp.equipment);
		s.place();
		return s;
	}

}