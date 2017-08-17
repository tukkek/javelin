package javelin.controller.scenario;

import java.util.ArrayList;

import javelin.controller.challenge.ChallengeRatingCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;

public class Campaign extends Scenario {
	/** Minimum starting party encounter level. */
	static final float INITIALEL = 5f;

	public Campaign() {
		allowallactors = true;
		allowkeys = true;
		allowlabor = true;
		asksquadnames = true;
		desertradius = 2;
		saveprefix = "campaign";
		fogofwar = true;
		haxor = true;
		helpfile = "How to play";
		linktowns = true;
		minigames = true;
		normalizemap = false;
		record = true;
		respawnlocations = true;
		size = 30;
		startingdungeons = Realm.values().length * 2;
		startingpopulation = 1;
		statictowns = false;
		towns = Realm.values().length;
		worldexploration = true;
		dominationwin = false;
		startingfeatures = size * size / 5;
		simpletroves = false;
	}

	@Override
	public void upgradesquad(ArrayList<Combatant> squad) {
		float startingcr = totalcr(squad);
		while (ChallengeRatingCalculator.calculateel(squad) < INITIALEL) {
			ArrayList<Upgrade> u = new ArrayList<Upgrade>();
			u.add(Commoner.SINGLETON);
			Combatant.upgradeweakest(squad, u);
		}
		Squad.active.gold = RewardCalculator
				.getgold(totalcr(squad) - startingcr);
	}

	static float totalcr(ArrayList<Combatant> squad) {
		int cr = 0;
		for (Combatant c : squad) {
			cr += ChallengeRatingCalculator.calculatecr(c.source);
		}
		return cr;
	}

	@Override
	public boolean checkfullsquad(ArrayList<Combatant> squad) {
		return ChallengeRatingCalculator.calculateel(squad) >= INITIALEL;
	}

	@Override
	public boolean win() {
		return false;
	}
}
