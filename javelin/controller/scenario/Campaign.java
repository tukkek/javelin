package javelin.controller.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.UnbalancedTeams;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.Realm;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;

public class Campaign extends Scenario {
	/** Minimum starting party encounter level. */
	public static final float INITIALEL;

	static {
		ArrayList<Float> crs = new ArrayList<Float>(4);
		for (int i = 0; i < 4; i++) {
			crs.add(1f);
		}
		try {
			INITIALEL = ChallengeCalculator.calculateelfromcrs(crs, false);
		} catch (UnbalancedTeams e) {
			throw new RuntimeException(e);
		}
	}

	public Campaign() {
		allowallactors = true;
		templekeys = true;
		allowlabor = true;
		asksquadnames = true;
		desertradius = 2;
		fogofwar = true;
		helpfile = "Campaign";
		easystartingtown = true;
		minigames = true;
		normalizemap = false;
		record = true;
		respawnlocations = true;
		size = 30;
		startingdungeons = 20;
		startingpopulation = 1;
		statictowns = false;
		towns = Realm.values().length;
		exploration = true;
		dominationwin = false;
		startingfeatures = size * size / 5;
		simpletroves = false;
		rewardbonus = 1;
		randomrealms = false;
		worlddistrict = false;
		spawn = true;
	}

	@Override
	public void upgradesquad(ArrayList<Combatant> squad) {
		float startingcr = totalcr(squad);
		while (ChallengeCalculator.calculateel(squad) < INITIALEL) {
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
			cr += ChallengeCalculator.calculatecr(c.source);
		}
		return cr;
	}

	@Override
	public boolean checkfullsquad(ArrayList<Combatant> squad) {
		return ChallengeCalculator.calculateel(squad) >= INITIALEL;
	}

	@Override
	public boolean win() {
		return false;
	}

	@Override
	public List<Location> generatelocations(World seed) {
		return Collections.EMPTY_LIST;
	}
}