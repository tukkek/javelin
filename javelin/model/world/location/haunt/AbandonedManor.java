package javelin.model.world.location.haunt;

import javelin.controller.map.haunt.AbandonedManorMap;
import javelin.controller.map.haunt.HauntMap;

public class AbandonedManor extends Haunt {
	public AbandonedManor() {
		super("Abandoned manor",
				new String[] { "Small animated object", "animated object",
						"Small monstrous spider", "monstrous spider",
						"Phantom fungus", "Ethereal marauder", "aranea",
						"Ethereal Filcher" });
	}

	@Override
	HauntMap getmap() {
		return new AbandonedManorMap();
	}
}
