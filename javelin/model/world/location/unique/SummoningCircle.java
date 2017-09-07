package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Monster;
import javelin.model.unit.abilities.spell.conjuration.Summon;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.labor.military.Academy;
import tyrant.mikera.engine.RPG;

/**
 * Lets you learn summoning spells. All monsters are theoretically possible but
 * offer just a few for higher randomization.
 *
 * @author alex
 */
public class SummoningCircle extends Academy {
	static final String DESCRIPTION = "Summoning circle";

	public static class BuildSummoningCircle extends BuildAcademy {
		public BuildSummoningCircle() {
			super(Rank.VILLAGE);
		}

		@Override
		protected Academy getacademy() {
			SummoningCircle goal = new SummoningCircle(true, 5);
			UniqueLocation.makecommon(goal, cost - 1, cost + 1);
			return goal;
		}
	}

	int maxspells;
	boolean outsidersonly;

	/** Constructor. */
	public SummoningCircle(boolean outsidersonly, int maxspells) {
		super(DESCRIPTION, DESCRIPTION, maxspells - 1, maxspells + 1,
				new HashSet<Upgrade>(), null, null);
		this.outsidersonly = outsidersonly;
		this.maxspells = maxspells;
		UniqueLocation.init(this);
		pillage = false;
		populate();
	}

	public SummoningCircle() {
		this(false, RPG.r(10, 15));
	}

	void populate() {
		ArrayList<Float> crs = new ArrayList<Float>(
				Javelin.MONSTERSBYCR.keySet());
		int tries = 1000;
		add: while (upgrades.size() < maxspells) {
			tries -= 1;
			if (tries == 0) {
				if (Javelin.DEBUG) {
					throw new RuntimeException(
							"too many summoning circle populaiton retries");
				}
				break;
			}
			float cr = RPG.pick(crs);
			for (Upgrade u : upgrades) {
				Summon s = (Summon) u;
				if (Javelin.getmonster(s.monstername).challengerating == cr) {
					continue add;
				}
			}
			Monster m = pickmonster(cr);
			if (m != null) {
				upgrades.add(new Summon(m.name, 1f));
			}
		}
	}

	Monster pickmonster(float cr) {
		List<Monster> monsters = Javelin.MONSTERSBYCR.get(cr);
		if (!outsidersonly) {
			return RPG.pick(monsters);
		}
		Collections.shuffle(monsters);
		for (Monster m : monsters) {
			if (m.type.equals("outsider")) {
				return m;
			}
		}
		return null;
	}

	@Override
	public void sort(ArrayList<Upgrade> upgrades) {
		upgrades.sort(new Comparator<Upgrade>() {
			@Override
			public int compare(Upgrade o1, Upgrade o2) {
				Summon a = (Summon) o1;
				Summon b = (Summon) o2;
				return a.monstername.compareTo(b.monstername);
			}
		});
	}

	@Override
	protected void generate() {
		while (x == -1 || Terrain.get(x, y).equals(Terrain.PLAIN)
				|| Terrain.get(x, y).equals(Terrain.HILL)) {
			super.generate();
		}
	}
}
