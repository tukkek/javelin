package javelin.controller.generator;

import java.util.ArrayList;
import java.util.Arrays;
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
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import tyrant.mikera.engine.RPG;

public class NpcGenerator {
	static final boolean DEBUG = Javelin.DEBUG && false;
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
					generateelite(RPG.pick(last), cr);
				}
			}
			last = tier;
		}
	}

	void generateelite(Monster m, float targetcr) {
		Float originalcr = m.cr;
		Combatant c = new Combatant(m, true);
		c.source.customName = "Elite " + c.source.name.toLowerCase();
		while (c.source.cr < targetcr && Commoner.SINGLETON.upgrade(c)) {
			ChallengeCalculator.calculatecr(c.source);
		}
		if (c.source.cr > originalcr) {
			register(c, c.source.getterrains());
		}
	}

	void generatenpcs() {
		for (float cr : getcrs()) {
			List<Monster> tier = Javelin.MONSTERSBYCR.get(cr);
			if (1 <= cr && cr <= MAXCR && !candidates.isEmpty()) {
				double npcs = gettarget(tier);
				for (int i = 0; i < npcs; i++) {
					Monster m = RPG.pick(candidates);
					Combatant c = generatenpc(m, cr);
					if (c != null) {
						register(c, Arrays.asList(Terrain.NONWATER));
					}
				}
			}
			if (tier != null) {
				for (Monster m : tier) {
					if (m.think(-1)) {
						candidates.add(m);
					}
				}
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

	public static final Combatant generatenpc(Monster m, float cr) {
		Kit k = RPG.pick(Kit.getpreferred(m));
		return generatenpc(m, k, cr);
	}

	public static Combatant generatenpc(Monster m, Kit k, float targetcr) {
		Float originalcr = m.cr;
		int tries = 10000;
		Combatant c = new Combatant(m, true);
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
				return null;
			}
		}
		if (c.source.cr <= originalcr) {
			return null;
		}
		c.source.customName = c.source.name + " "
				+ k.gettitle(c.source).toLowerCase();
		return c;
	}

	void register(Combatant c, List<?> terrains) {
		if (c.source.isaquatic()) {
			return;
		}
		c.elite = true;
		for (Object t : terrains) {
			ArrayList<Combatant> encounter = new ArrayList<Combatant>(1);
			encounter.add(c);
			Organization.ENCOUNTERSBYTERRAIN.get(t.toString().toLowerCase())
					.put(ChallengeCalculator.crtoel(c.source.cr),
							new Encounter(encounter));
			totalregistered += 1;
		}
	}
}
