package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.world.QueueItem;
import javelin.model.world.Squad;
import javelin.model.world.Town;
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
		newrecruit: while (town.recruits.size() < RECRUITSPERTOWN) {
			RecruitOption recruit = getmonster();
			for (Town t : Town.towns) {
				if (t.recruits.contains(recruit)) {
					continue newrecruit;
				}
			}
			town.recruits.add(recruit);
		}
		ArrayList<Option> options = new ArrayList<Option>();
		for (int i = 0; i < RECRUITSPERTOWN; i++) {
			options.add(town.recruits.get(i));
		}
		return options;
	}

	@Override
	public boolean select(Option o) {
		if (!super.select(o)) {
			return false;
		}
		final RecruitOption rop = (RecruitOption) o;
		town.recruits.remove(rop);
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
			for (QueueItem upgrading : t.training.queue) {
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
		final String nametext = "Give a name to your " + m.name
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
		m.customName = name;
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
}
