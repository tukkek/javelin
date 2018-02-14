package javelin.model.world.location.dungeon.temple;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.terrain.Plains;
import javelin.controller.terrain.Terrain;
import javelin.model.Realm;
import javelin.model.item.relic.Ankh;
import javelin.model.unit.Monster;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Feature;
import javelin.model.world.location.dungeon.temple.features.Spirit;
import tyrant.mikera.engine.RPG;

/**
 * Found resting in the {@link Plains}. Evil enemies never found here. 1-3 good
 * spirits can tell show you the location of an undiscovered {@link Feature}.
 * 
 * @see Temple
 * @see Monster#good
 * @author alex
 */
public class GoodTemple extends Temple {
	private static final String FLUFF = "The bizarre and tall complex seems to be carved entirely out of ivory and white stones.\n"
			+ "Despite being in no place of particular importance the common animals seem to avoid it.\n"
			+ "In fact, the eerie silence around the entire place makes you wonder if this is truly happening or only a fleeting dream.\n"
			+ "You enter the holy ground, daring say nothing as you breath deeply in anticipation of the vistas inside.";

	/** Constructor. */
	public GoodTemple(Integer pop) {
		super(Realm.GOOD, pop, new Ankh(), FLUFF);
		terrain = Terrain.PLAIN;
		floor = "terrainarena";
		wall = "terraindungeonwall";
	}

	@Override
	public boolean validate(ArrayList<Combatant> foes) {
		for (Combatant c : foes) {
			if (Boolean.TRUE.equals(c.source.good)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Feature> getfeatures(Dungeon d) {
		ArrayList<Feature> spirits = new ArrayList<Feature>();
		int nbraziers = RPG.r(5, 7);
		for (int i = 0; i < nbraziers; i++) {
			Point spot = d.findspot();
			spirits.add(new Spirit(spot.x, spot.y));
		}
		return spirits;
	}
}
