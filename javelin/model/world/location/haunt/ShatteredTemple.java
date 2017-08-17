package javelin.model.world.location.haunt;

import javelin.controller.map.haunt.HauntMap;
import javelin.controller.map.haunt.ShatteredTempleMap;

public class ShatteredTemple extends Haunt {
	public ShatteredTemple() {
		super("Shaterred temple", new String[] { "Tiefling", "Aasimar", "Satyr",
				"Satyr with pipes", "azer", "Janni" });
	}

	@Override
	HauntMap getmap() {
		return new ShatteredTempleMap();
	}
}
