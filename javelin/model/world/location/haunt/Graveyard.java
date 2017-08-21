package javelin.model.world.location.haunt;

import javelin.controller.map.haunt.GraveyardMap;
import javelin.controller.map.haunt.HauntMap;

public class Graveyard extends Haunt {

	public Graveyard() {
		super("Graveyard",
				new String[] { "zombie", "Small skeleton", "Skeleton" });
		elmodifier = +5;
	}

	@Override
	HauntMap getmap() {
		return new GraveyardMap();
	}
}
