package javelin.view.screen.haxor;

import java.util.ArrayList;

import javelin.controller.action.world.CastSpells;
import javelin.model.unit.Combatant;
import javelin.model.world.Merchant;
import javelin.model.world.WorldActor;
import javelin.model.world.place.Lair;
import javelin.model.world.place.Portal;
import javelin.model.world.place.dungeon.Dungeon;
import javelin.model.world.place.guarded.Inn;
import javelin.model.world.place.guarded.Shrine;
import javelin.model.world.place.town.Town;
import javelin.model.world.place.unique.Haxor;
import tyrant.mikera.engine.RPG;

/**
 * Generate a {@link WorldActor} near {@link Haxor}
 * 
 * @author alex
 */
public class Materialize extends Hax {
	public Materialize(String name, double price, boolean requirestargetp) {
		super(name, price, requirestargetp);
	}

	@Override
	protected boolean hack(Combatant target, HaxorScreen s) {
		ArrayList<String> summonable = new ArrayList<String>();
		summonable.add(0, "Dungeon");
		summonable.add(1, "Inn");
		summonable.add(2, "Lair");
		summonable.add(3, "Merchant");
		summonable.add(4, "Portal");
		summonable.add(5, "Shrine");
		int choice =
				CastSpells.choose("Materialize what", summonable, true, true);
		WorldActor materialize;
		if (choice == 0) {
			materialize = new Dungeon();
		} else if (choice == 1) {
			materialize = new Inn();
		} else if (choice == 2) {
			materialize = new Lair();
		} else if (choice == 3) {
			materialize = new Merchant();
		} else if (choice == 4) {
			materialize = new Portal(Haxor.singleton,
					RPG.pick(WorldActor.getall(Town.class)));
		} else if (choice == 5) {
			materialize = new Shrine();
		} else {
			throw new RuntimeException(
					"don't know what to materialize #haxorscreen");
		}
		s.generate(materialize);
		return true;
	}
}