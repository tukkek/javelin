package javelin.model.world.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.controller.map.location.haunt.AbandonedManorMap;

public class AbandonedManor extends Haunt {
	public AbandonedManor() {
		super("Abandoned manor", 5, 10,
				new String[] { "Small animated object", "animated object",
						"Small monstrous spider", "monstrous spider",
						"Phantom fungus", "Ethereal marauder", });
	}

	@Override
	LocationMap getmap() {
		return new AbandonedManorMap();
	}
}
