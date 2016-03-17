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
import javelin.model.world.Squad;
import javelin.model.world.town.Order;
import javelin.model.world.town.Town;
import javelin.view.screen.IntroScreen;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.RecruitOption;
import tyrant.mikera.tyrant.InfoScreen;

/**
 * Adds a new {@link Combatant} to the {@link Squad}.
 * 
 * @author alex
 */
public class RecruitScreen extends PurchaseScreen {

	public static final int RECRUITSPERTOWN = 5;

	public RecruitScreen(String s, Town t) {
		super(s, t);
	}

	@Override
	public List<Option> getOptions() {
		return new ArrayList<Option>(town.lairs);
	}

	@Override
	public boolean select(Option o) {
		if (!super.select(o)) {
			return false;
		}
		final RecruitOption rop = (RecruitOption) o;
		town.lairs.remove(rop);
		rop.m.hd.roll(rop.m);
		Javelin.recruit(rop.m);
		return true;
	}

	public static boolean checkforduplicate(String name) {
		for (final Squad s : Squad.squads) {
			for (final Combatant namesake : s.members) {
				if (namesake.toString().equals(name)) {
					return true;
				}
			}
		}
		for (Town t : Town.towns) {
			for (Order upgrading : t.training.queue) {
				if (upgrading.payload.toString().equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * TODO refactor into class
	 */
	static public void namingscreen(final Monster m) {
		m.customName = namingscreen(m.name);
	}

	public static String namingscreen(String nametext) {
		nametext = "Give a name to your " + nametext
				+ "! Press BACKSPACE to erase.\n\n";
		final IntroScreen namescreen = new IntroScreen(nametext);
		String name = "";
		char f;
		while (true) {
			f = InfoScreen.feedback();
			if (f == '\n') {
				if (!name.isEmpty()) {
					if (RecruitScreen.checkforduplicate(name)) {
						continue;
					} else {
						break;
					}
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

	static public RecruitOption getmonster() {
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

	private int sumxp() {
		BigDecimal sum = new BigDecimal(0);
		for (Combatant c : Squad.active.members) {
			sum = sum.add(c.xp);
		}
		return Math.round(sum.floatValue() * 100);
	}

	@Override
	protected boolean canbuy(Option o) {
		return o.price <= sumxp();
	}

	@Override
	protected void spend(Option o) {
		double cost = o.price / 100;
		double percapita = cost / new Float(Squad.active.size());
		boolean buyfromall = true;
		for (Combatant c : Squad.active.members) {
			if (c.xp.doubleValue() < percapita) {
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
			Collections.sort(squad, new Comparator<Combatant>() {
				@Override
				public int compare(Combatant o1, Combatant o2) {
					return o2.xp.compareTo(o1.xp);
				}
			});
			for (Combatant c : squad) {
				if (c.xp.doubleValue() >= cost) {
					c.xp = c.xp.subtract(new BigDecimal(cost));
					return;
				}
				cost -= c.xp.doubleValue();
				c.xp = new BigDecimal(0);
			}
		}
	}
}
