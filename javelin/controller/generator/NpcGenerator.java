package javelin.controller.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.kit.Kit;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Commoner;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import tyrant.mikera.engine.RPG;

public class NpcGenerator {
	static final boolean DEBUG = Javelin.DEBUG && true;
	static final int MAXCR = 25;
	static final double RATIO = .1;

	ArrayList<Monster> candidates = new ArrayList<Monster>();
	int totalregistered = 0;

	public void generate() {
		generatenpcs();
		generateelites();
		if (DEBUG) {
			float total = 0;
			for (EncounterIndex encounters : Organization.ENCOUNTERSBYTERRAIN
					.values()) {
				total += encounters.count();
			}
			int percent = Math.round(100 * totalregistered / total);
			System.out.println("Total registered: " + totalregistered + " ("
					+ percent + "%)");
		}
	}

	void generateelites() {
		List<Monster> last = null;
		for (float cr : getcrs()) {
			if (cr >= 5) {
				return;
			}
			List<Monster> tier = Javelin.MONSTERSBYCR.get(cr);
			if (tier == null) {
				continue;
			}
			if (last != null) {
				double elites = gettarget(tier);
				for (int i = 0; i < elites; i++) {
					upgradeelite(RPG.pick(last), cr);
				}
			}
			last = tier;
		}
	}

	void upgradeelite(Monster m, float targetcr) {
		Combatant c = new Combatant(m, true);
		c.source.customName = "Elite " + c.source.name.toLowerCase();
		while (c.source.cr < targetcr && Commoner.SINGLETON.upgrade(c)) {
			ChallengeCalculator.calculatecr(c.source);
		}
		register(c);
	}

	void generatenpcs() {
		for (float cr : getcrs()) {
			List<Monster> tier = Javelin.MONSTERSBYCR.get(cr);
			if (1 <= cr && cr <= MAXCR && !candidates.isEmpty()) {
				double npcs = gettarget(tier);
				for (int i = 0; i < npcs; i++) {
					generatenpc(RPG.pick(candidates), cr);
				}
			}
			if (tier != null) {
				registercandidates(tier);
			}
		}
	}

	double gettarget(List<Monster> tier) {
		double npcs;
		if (tier == null || tier.size() < 2) {
			npcs = 2;
		} else {
			npcs = Math.max(1, tier.size() * RATIO);
		}
		return npcs;
	}

	TreeSet<Float> getcrs() {
		TreeSet<Float> crs = new TreeSet<Float>(Javelin.MONSTERSBYCR.keySet());
		for (int cr = 1; cr <= MAXCR; cr++) {
			crs.add(new Float(cr));
		}
		return crs;
	}

	void generatenpc(Monster m, float targetcr) {
		int tries = 10000;
		Combatant c = new Combatant(m, true);
		Kit k = RPG.pick(Kit.gerpreferred(m));
		c.source.customName = m.name + " " + k.name.toLowerCase();
		float base = c.source.cr + targetcr / 2;
		while (c.source.cr < base && k.classlevel.apply(c)) {
			ChallengeCalculator.calculatecr(c.source);
		}
		HashSet<Upgrade> upgrades = new HashSet<Upgrade>(k.basic);
		upgrades.addAll(k.extension);
		while (c.source.cr < targetcr) {
			c.upgrade(upgrades);
			tries -= 1;
			if (tries == 0) {
				return;
			}
		}
		register(c);
	}

	void register(Combatant c) {
		if (c.source.isaquatic()) {
			return;
		}
		for (Terrain t : Terrain.NONWATER) {
			ArrayList<Combatant> encounter = new ArrayList<Combatant>(1);
			encounter.add(c);
			Organization.ENCOUNTERSBYTERRAIN.get(t.name.toLowerCase()).put(
					ChallengeCalculator.crtoel(c.source.cr),
					new Encounter(encounter));
			totalregistered += 1;
		}
		if (DEBUG) {
			System.out.println(c + " cr" + c.source.cr);
		}
	}

	void registercandidates(List<Monster> tier) {
		for (Monster m : tier) {
			if (m.think(-1)) {
				candidates.add(m);
			}
		}
	}
}
