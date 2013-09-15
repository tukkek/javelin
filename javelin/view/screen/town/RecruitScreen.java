package javelin.view.screen.town;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.model.unit.Monster;
import javelin.model.world.Town;
import javelin.view.screen.town.option.Option;
import javelin.view.screen.town.option.RecruitOption;

public class RecruitScreen extends PurchaseScreen {

	public static final int RECRUITSPERTOWN = 5;

	public RecruitScreen(String s, Town t) {
		super(s, t);
	}

	@Override
	List<Option> getOptions() {
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
	boolean select(Option o) {
		if (!super.select(o)) {
			return false;
		}
		final RecruitOption rop = (RecruitOption) o;
		town.recruits.remove(rop);
		/* TODO do we really want max hp here? */
		Javelin.recruit(rop.m);
		return true;
	}

	static final Random random = new Random();

	static public RecruitOption getmonster() {
		final Monster candidate = Javelin.ALLMONSTERS.get(random
				.nextInt(Javelin.ALLMONSTERS.size()));
		if (ChallengeRatingCalculator.calculateCr(candidate) < 2) {
			return getmonster();
		}
		float basecost = (ChallengeRatingCalculator.calculateCr(candidate) - 1) / .2f;
		long cost = Math.round(100f * Math.pow(basecost, 3f));
		return new RecruitOption(candidate.name, cost, candidate);
	}
}
