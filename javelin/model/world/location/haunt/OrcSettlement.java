package javelin.model.world.location.haunt;

import javelin.controller.map.haunt.HauntMap;
import javelin.controller.map.haunt.OrcSettlementMap;

public class OrcSettlement extends Haunt {
	public OrcSettlement() {
		super("Orc settlement", new String[] { "Kobold", "Goblin", "Orc",
				"Half orc", "Hobgoblin" });
		elmodifier = +4;
	}

	@Override
	HauntMap getmap() {
		return new OrcSettlementMap();
	}
}
