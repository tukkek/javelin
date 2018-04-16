package javelin.model.world.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.controller.map.location.haunt.SunkenShipMap;
import javelin.controller.terrain.Terrain;

public class SunkenShip extends Haunt {
	public SunkenShip() {
		super("Sunken ship", 5, 10,
				new String[] { "Aquatic elf", "merfolk", "Locathah", "octopus",
						"Skum", "Sahuagin", "Malenti", "Sahuagin mutant" });
	}

	@Override
	LocationMap getmap() {
		return new SunkenShipMap();
	}

	@Override
	protected void generate() {
		x = -1;
		while (x == -1 || !Terrain.get(x, y).equals(Terrain.WATER)) {
			generate(this, true);
		}
	}
}
