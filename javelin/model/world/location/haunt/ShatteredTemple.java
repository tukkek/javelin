package javelin.model.world.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.controller.map.location.haunt.ShatteredTempleMap;

public class ShatteredTemple extends Haunt {
	public ShatteredTemple() {
		super("Shaterred temple", 10, 15, new String[] { "Tiefling", "Aasimar",
				"Satyr", "Satyr with pipes", "azer", "Janni" });
	}

	@Override
	LocationMap getmap() {
		return new ShatteredTempleMap();
	}
}
