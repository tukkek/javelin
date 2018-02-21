package javelin.controller.fight.minigame.arena.building;

import javelin.model.unit.attack.Combatant;

public class ArenaLair extends ArenaBuilding {

	public ArenaLair() {
		super("Lair", "locationmercenariesguild",
				"Click this lair to recruit allies into the arena!");
	}

	@Override
	protected boolean click(Combatant current) {
		return true;
	}

}
