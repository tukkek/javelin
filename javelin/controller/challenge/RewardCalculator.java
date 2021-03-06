package javelin.controller.challenge;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javelin.controller.DescendingLevelComparator;
import javelin.model.item.Item;
import javelin.model.item.ItemSelection;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Chest;
import javelin.model.world.location.dungeon.Dungeon;
import tyrant.mikera.engine.RPG;

/**
 * Determines experience points and treasure to be awarded after winning a
 * battle. Rules for this are found in the core d20 rules and also on Upper
 * Krust's work which is repackaged with permition on the 'doc' directory.
 * 
 * @author alex
 */
public class RewardCalculator {
	private static class TableLine {
		final double a, b, c, d, e, f, g, h;

		public TableLine(final double a, final double b, final double c,
				final double d, final double e, final double f, final double g,
				final double h) {
			super();
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
			this.e = e;
			this.f = f;
			this.g = g;
			this.h = h;
		}

		public double getValue(final int partysize) {
			if (partysize == 1) {
				return a;
			}
			if (partysize == 2) {
				return b;
			}
			if (partysize == 3) {
				return c;
			}
			if (partysize <= 5) {
				return d;
			}
			if (partysize <= 7) {
				return e;
			}
			if (partysize <= 11) {
				return f;
			}
			if (partysize <= 15) {
				return g;
			}
			if (partysize <= 23) {
				return h;
			}
			throw new RuntimeException("Can't calculate party of more than 23");
		}
	}

	static Map<Integer, TableLine> table = new TreeMap<Integer, TableLine>();

	static {
		table.put(-12, new TableLine(18.75, 9.375, 6.25, 4.6875, 3.125, 2.34375,
				1.5625, 1.171875));
		table.put(-11, new TableLine(25, 12.5, 9.375, 6.25, 4.6875, 3.125,
				2.34375, 1.5625));
		table.put(-10, new TableLine(37.5, 18.75, 12.5, 9.375, 6.250, 4.6875,
				3.125, 2.34375));
		table.put(-9, new TableLine(50, 25, 18.75, 12.5, 9.375, 6.250, 4.6875,
				3.125));
		table.put(-8,
				new TableLine(75, 37.5, 25, 18.75, 12.5, 9.375, 6.250, 4.6875));
		table.put(-7,
				new TableLine(100, 50, 37.5, 25, 18.75, 12.5, 9.3750, 6.25));
		table.put(-6,
				new TableLine(150, 75, 50, 37.5, 25, 18.75, 12.50, 9.375));
		table.put(-5, new TableLine(200, 100, 75, 50, 37.5, 25, 18.75, 12.5));
		table.put(-4, new TableLine(300, 150, 100, 75, 50, 37.5, 25, 18.75));
		table.put(-3, new TableLine(400, 200, 150, 100, 75, 50, 37.5, 25));
		table.put(-2, new TableLine(600, 300, 200, 150, 100, 75, 50, 37.5));
		table.put(-1, new TableLine(800, 400, 300, 200, 150, 100, 75, 50));
		table.put(0, new TableLine(1200, 600, 400, 300, 200, 150, 100, 75));
		table.put(1, new TableLine(1600, 800, 600, 400, 300, 200, 150, 100));
		table.put(2, new TableLine(2400, 1200, 800, 600, 400, 300, 200, 150));
		table.put(3, new TableLine(3200, 1600, 1200, 800, 600, 400, 300, 200));
		table.put(4, new TableLine(4800, 2400, 1600, 1200, 800, 600, 400, 300));
	}

	static private double getexperiencepercharacter(int eldifference,
			final int nsurvivors) {
		if (eldifference > 4) {
			eldifference = 4;
		} else if (eldifference < -12) {
			eldifference = -12;
		}
		return table.get(eldifference).getValue(nsurvivors);
	}

	static double getpartycr(final int eldifference, final int nsurvivors) {
		return nsurvivors * .8
				* getexperiencepercharacter(eldifference, nsurvivors) / 1000.0;
	}

	/**
	 * @param team
	 *            Opponent force defeated.
	 * @return sum of gold this battle should reward.
	 * @see #getgold(float)
	 */
	public static int receivegold(final List<Combatant> team) {
		int sum = 0;
		for (final Combatant m : team) {
			sum += getgold(ChallengeRatingCalculator.calculateCr(m.source));
		}
		return sum;
	}

	/**
	 * @param cr
	 *            Given a challenge rating...
	 * @return gold treasure reward for such an opponent.
	 */
	public static int getgold(final float cr) {
		return cr > 0 ? Math.round(cr * cr * cr * 7.5f) : 0;
	}

	/**
	 * @param gold
	 *            Given a certain amount of gold...
	 * @return the challenge rating that would warrant this treasure.
	 */
	public static int getcr(final float gold) {
		return Math.round(Math.round(Math.cbrt(gold / 7.5f)));
	}

	/**
	 * @param gold
	 *            Value to be added in gold or {@link Item}s.
	 * @param x
	 *            {@link Dungeon} coordinate.
	 * @param y
	 *            {@link Dungeon} coordinate.
	 * @return A {@link Dungeon} chest.
	 */
	public static Chest createchest(int gold, int x, int y) {
		ItemSelection chest = new ItemSelection();
		if (RPG.r(1, 2) == 1) {// 50% are gold and 50% are item
			int limit = gold / 10;
			for (Integer price : Item.BYPRICE.descendingMap().keySet()) {
				if (price <= limit) {
					break;
				}
				if (gold > price) {
					gold -= price;
					chest.add(RPG.pick(Item.BYPRICE.get(price)));
				}
			}
		}
		return new Chest("chest", x, y, gold, chest);
	}

	/**
	 * Calculates proper experience reward for a given battle and distributes in
	 * a non-uniform manner. d20 level caps are exponential but since Javelin
	 * uses challenge rating as XP it becomes more linear - to circumvent that
	 * this method distributes 1 xp part to the strongest unit (including it's
	 * xp bank), 2 to the second strongest, etc. This may look random at first
	 * but in the long run ensures {@link Combatant}s will level up in a
	 * balanced manner, with lower level units gaining levels faster than
	 * already strong units - which is what the d20 system is designed to do.
	 * 
	 * @param winners
	 *            {@link Combatant}s to award XP to.
	 * @param originalblue
	 *            Allied team that started the battle.
	 * @param originalred
	 *            Battle opponent.
	 * @param bonus
	 *            Multiplier bonus.
	 * @return A string representing how much XP was gained by the party.
	 * @see Combatant#xp
	 */
	public static String rewardxp(ArrayList<Combatant> winners,
			List<Combatant> originalblue, List<Combatant> originalred,
			int bonus) {
		int eldifference =
				Math.round(ChallengeRatingCalculator.calculateel(originalred)
						- ChallengeRatingCalculator.calculateel(originalblue));
		double partycr = getpartycr(eldifference, winners.size()) * bonus;
		distributexp(winners, partycr);
		return "Party wins "
				+ new BigDecimal(100 * partycr).setScale(0, RoundingMode.UP)
				+ "XP!";
	}

	/**
	 * @param units
	 *            {@link Combatant}s to receive experience.
	 * @param xp
	 *            Total amount of experience to be distributed.
	 * @see #rewardxp(ArrayList, List, List, int)
	 */
	public static void distributexp(ArrayList<Combatant> units, double xp) {
		List<Combatant> survivors = (List<Combatant>) units.clone();
		Collections.sort(survivors, new DescendingLevelComparator());
		float segments = 0;
		for (int i = 1; i <= survivors.size(); i++) {
			segments += i;
		}
		for (int i = 1; i <= survivors.size(); i++) {
			final Combatant survivor = survivors.get(i - 1);
			survivor.xp = survivor.xp.add(new BigDecimal(xp * i / segments)
					.setScale(2, RoundingMode.HALF_UP));
		}
	}
}
