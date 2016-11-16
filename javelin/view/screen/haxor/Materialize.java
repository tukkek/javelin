package javelin.view.screen.haxor;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.model.unit.Combatant;
import javelin.model.world.Caravan;
import javelin.model.world.WorldActor;
import javelin.model.world.location.Lair;
import javelin.model.world.location.Portal;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.fortification.Guardian;
import javelin.model.world.location.fortification.Shrine;
import javelin.model.world.location.town.Dwelling;
import javelin.model.world.location.town.Inn;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.unique.Haxor;
import tyrant.mikera.engine.RPG;

/**
 * Generate a {@link WorldActor} near {@link Haxor}.
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
		summonable.add(4, "Lair");
		summonable.add(5, "Merchant");
		summonable.add(6, "Portal");
		summonable.add(7, "Shrine");
		int choice = Javelin.choose("Materialize what", summonable, true, true);
		WorldActor materialize;
		if (choice == 0) {
			materialize = new Dungeon();
		} else if (choice == 1) {
			materialize = new Dwelling();
		} else if (choice == 2) {
			materialize = new Guardian();
		} else if (choice == 3) {
			materialize = new Inn();
		} else if (choice == 4) {
			materialize = new Lair();
		} else if (choice == 5) {
			materialize = new Caravan();
		} else if (choice == 6) {
			materialize = new Portal(Haxor.singleton,
					RPG.pick(WorldActor.getall(Town.class)));
		} else if (choice == 7) {
			materialize = new Shrine();
		} else {
			throw new RuntimeException(
					"don't know what to materialize #haxorscreen");
		}
		s.generate(materialize);
		return true;
	}
}