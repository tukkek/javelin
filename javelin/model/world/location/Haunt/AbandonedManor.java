package javelin.model.world.location.Haunt;

import javelin.controller.map.Map;
import javelin.controller.map.haunt.AbandonedManorMap;

public class AbandonedManor extends Haunt {
	public AbandonedManor() {
		super("Abandoned manor",
				new String[] { "Small animated object", "animated object",
						"Small monstrous spider", "monstrous spider",
						"Phantom fungus", "Ethereal marauder", "aranea",
						"Ethereal Filcher" });
	}

	@Override
	Map getmap() {
		return new AbandonedManorMap();
	}
}
