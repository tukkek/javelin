package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.model.spell.Summon;
import javelin.model.world.location.town.Academy;
import tyrant.mikera.engine.RPG;

/**
 * Lets you learn summoning spells. All monsters are theoretically possible but
 * offer just a few for higher randomization.
 * 
 * @author alex
 */
public class SummoningCircle extends Academy {
	static final String DESCRIPTION = "Summoning circle";

	/** Constructor. */
	public SummoningCircle() {
		super(DESCRIPTION, DESCRIPTION, 5, 15, new HashSet<Upgrade>(), null,
				null);
		UniqueLocation.init(this);
		pillage = false;
		populate();
	}

	void populate() {
		ArrayList<Float> crs = new ArrayList<Float>(
				Javelin.MONSTERSBYCR.keySet());
		add: while (upgrades.size() < 5) {
			float cr = RPG.pick(crs);
			for (Upgrade u : upgrades) {
				Summon s = (Summon) u;
				if (Javelin.getmonster(s.monstername).challengerating == cr) {
					continue add;
				}
			}
			upgrades.add(new Summon(RPG.pick(Javelin.MONSTERSBYCR.get(cr)).name,
					1f));
		}
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
