package javelin.controller.fight.minigame.arena.building;

import javelin.model.unit.attack.Combatant;

public class ArenaAcademy extends ArenaBuilding {

	public ArenaAcademy() {
		super("Academy", "locationrealmacademy",
				"Click this academy to upgrade the active unit!");
	}

	@Override
	protected boolean click(Combatant current) {
		return true;
	}

	@Override
	public String getactiondescription(Combatant current) {
		return super.getactiondescription(current) + "\n\n" + current
				+ " currently has " + current.gethumanxp() + ".";
	}
}
