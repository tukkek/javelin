package javelin.controller.fight;

import javelin.model.world.place.WorldPlace;

public class TownSiege extends Siege {
	public TownSiege(WorldPlace town) {
		super(town);
	}

	@Override
	public boolean canbribe() {
		/*
		 * TODO doesn't make much sense but potentially could enable it. Needs
		 * to Town#capture after the battle is over though.
		 */
		return false;
	}
}
