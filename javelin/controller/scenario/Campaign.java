package javelin.controller.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javelin.controller.challenge.CrCalculator;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.Realm;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.World;
import javelin.model.world.location.Location;

public class Campaign extends Scenario {
	/** Minimum starting party encounter level. */
	public static final float INITIALEL = 5f;

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
		worldexploration = true;
		dominationwin = false;
		startingfeatures = size * size / 5;
		simpletroves = false;
		rewardbonus = 1;
		randomrealms = false;
		worlddistrict = false;
	}

	@Override
	public void upgradesquad(ArrayList<Combatant> squad) {
		float startingcr = totalcr(squad);
		while (CrCalculator.calculateel(squad) < INITIALEL) {
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
			cr += CrCalculator.calculatecr(c.source);
		}
		return cr;
	}

	@Override
	public boolean checkfullsquad(ArrayList<Combatant> squad) {
		return CrCalculator.calculateel(squad) >= INITIALEL;
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