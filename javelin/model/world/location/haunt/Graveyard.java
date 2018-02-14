package javelin.model.world.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.controller.map.location.haunt.GraveyardMap;

public class Graveyard extends Haunt {

	public Graveyard() {
		super("Graveyard", new String[] { "zombie", "Small skeleton",
				"Skeleton", "ghoul", "ghast" });
		elmodifier = +5;
	}

	@Override
	LocationMap getmap() {
		return new GraveyardMap();
	}
}
