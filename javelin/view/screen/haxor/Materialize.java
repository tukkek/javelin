package javelin.view.screen.haxor;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.world.Caravan;
import javelin.model.world.World;
import javelin.model.unit.attack.Combatant;
import javelin.model.world.Actor;
import javelin.model.world.location.Portal;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.fortification.Guardian;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Dwelling;
import javelin.model.world.location.town.labor.basic.Lodge;
import javelin.model.world.location.town.labor.religious.Shrine;
import javelin.model.world.location.unique.Haxor;
import tyrant.mikera.engine.RPG;

/**
 * Generate a {@link Actor} near {@link Haxor}.
 *
 * @author alex
 */
public class Materialize extends Hax {
	/** See {@link Hax#Hax(String, double, boolean)}. */
	public Materialize(String name, Character keyp, double price,
			boolean requirestargetp) {
		super(name, keyp, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		ArrayList<String> summonable = new ArrayList<String>();
		summonable.add(0, "Dungeon");
		summonable.add(1, "Dwelling");
		summonable.add(2, "Guardian");
		summonable.add(3, "Inn");
		// summonable.add(4, "Lair");
		summonable.add(4, "Merchant");
		summonable.add(5, "Portal");
		summonable.add(6, "Shrine");
		int choice = Javelin.choose("Materialize what", summonable, true, true);
		Actor materialize;
		if (choice == 0) {
			materialize = new Dungeon();
		} else if (choice == 1) {
			materialize = new Dwelling();
		} else if (choice == 2) {
			materialize = new Guardian();
		} else if (choice == 3) {
			materialize = new Lodge();
			// } else if (choice == 4) {
			// materialize = new Lair();
		} else if (choice == 4) {
			materialize = new Caravan();
		} else if (choice == 5) {
			materialize = new Portal(Haxor.singleton,
					RPG.pick(World.getall(Town.class)));
		} else if (choice == 6) {
			materialize = new Shrine(2);
		} else {
			throw new RuntimeException(
					"don't know what to materialize #haxorscreen");
		}
		s.generate(materialize);
		return true;
	}
}