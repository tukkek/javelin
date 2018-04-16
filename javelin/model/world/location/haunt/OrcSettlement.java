package javelin.model.world.location.haunt;

import javelin.controller.map.location.LocationMap;
import javelin.controller.map.location.haunt.OrcSettlementMap;

public class OrcSettlement extends Haunt {
	public OrcSettlement() {
		super("Orc settlement", 5, 10, new String[] { "Kobold", "Goblin", "Orc",
				"Half orc", "Hobgoblin" });
		elmodifier = +4;
	}

	@Override
	LocationMap getmap() {
		return new OrcSettlementMap();
	}
}
