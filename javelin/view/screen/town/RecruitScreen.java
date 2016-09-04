package javelin.view.screen.town;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.Option;
import javelin.view.screen.town.option.RecruitOption;

/**
 * Adds a new {@link Combatant} to the {@link Squad}.
 * 
 * @author alex
 */
public class RecruitScreen extends PurchaseScreen {
	/** See {@link PurchaseScreen#PurchaseScreen(String, Town)}. */
	public RecruitScreen(String s, Town t) {
		super(s, t);
	}

	@Override
	public List<Option> getoptions() {
		return new ArrayList<Option>(town.lairs);
	}

	@Override
	public boolean select(Option o) {
		if (!super.select(o)) {
			return false;
		}
		final RecruitOption rop = (RecruitOption) o;
		rop.m.hd.roll(rop.m);
		Javelin.recruit(rop.m);
		return true;
	}

	/**
	 * @param currentName
	 *            Given an object's current name...
	 * @return a new user-inputted name for it.
	 */
	static public String namingscreen(final String currentName) {
		String nametext = "Give a new name to " + currentName + ": ";
		final IntroScreen namescreen = new IntroScreen(nametext);
		String name = "";
		char f;
		while (true) {
			f = InfoScreen.feedback();
			if (f == '\n') {
				if (!name.isEmpty()) {
					break;
				}
			}
			if (!(f == '\b' || f == ' ' || Character.isLetterOrDigit(f))) {
				continue;
			}
			if (f == '\b') {
				if (!name.isEmpty()) {
					name = name.substring(0, name.length() - 1);
				}
			} else {
				name = name + f;
			}
			namescreen.text = nametext + name;
			namescreen.repaint();
		}
		return name;
	}

	static final Random random = new Random();

	static RecruitOption getmonster() {
		final Monster candidate = Javelin.ALLMONSTERS
				.get(random.nextInt(Javelin.ALLMONSTERS.size()));
		if (ChallengeRatingCalculator.calculateCr(candidate) < 2) {
			return getmonster();
		}
		float basecost =
				(ChallengeRatingCalculator.calculateCr(candidate) - 1) / .2f;
		return new RecruitOption(candidate.name,
				Math.round(100f * Math.pow(basecost, 3f)), candidate);
	}

	@Override
	public String printpriceinfo(Option o) {
		return " " + Math.round(Math.ceil(o.price)) + "XP";
	}

	@Override
	public String printInfo() {
		return "Your squad has " + sumxp() + "XP";
	}

	/**
	 * @return Total of XP between all active {@link Squad} members.
	 */
	public static int sumxp() {
		BigDecimal sum = new BigDecimal(0);
		for (Combatant c : Squad.active.members) {
			sum = sum.add(c.xp);
		}
		return Math.round(sum.floatValue() * 100);
	}

	@Override
	protected boolean canbuy(Option o) {
		return canbuy(o.price);
	}

	/**
	 * @param price
	 *            Price in XP (100XP = 1CR).
	 * @return <code>true</code> if currently active {@link Squad} can afford
	 *         this much.
	 */
	static public boolean canbuy(double price) {
		return price <= sumxp();
	}

	@Override
	protected void spend(Option o) {
		spend(o.price / 100);
	}

	/**
	 * @param cr
	 *            Spend this much CR in recruiting a rookie (1CR = 100XP).
	 */
	static public void spend(double cr) {
		double percapita = cr / new Float(Squad.active.members.size());
		boolean buyfromall = true;
		for (Combatant c : Squad.active.members) {
			if (percapita > c.xp.doubleValue()) {
				buyfromall = false;
				break;
			}
		}
		if (buyfromall) {
			for (Combatant c : Squad.active.members) {
				c.xp = c.xp.subtract(new BigDecimal(percapita));
			}
		} else {
			ArrayList<Combatant> squad =
					new ArrayList<Combatant>(Squad.active.members);
			ChallengeRatingCalculator.calculateel(squad);
			Collections.sort(squad, new Comparator<Combatant>() {
				@Override
				public int compare(Combatant o1, Combatant o2) {
					final float cr1 =
							o2.xp.floatValue() + o2.source.challengeRating;
					final float cr2 =
							o1.xp.floatValue() + o1.source.challengeRating;
					return new Float(cr1).compareTo(cr2);
				}
			});
			for (Combatant c : squad) {
				if (c.xp.doubleValue() >= cr) {
					c.xp = c.xp.subtract(new BigDecimal(cr));
					return;
				}
				cr -= c.xp.doubleValue();
				c.xp = new BigDecimal(0);
			}
		}
	}
}
