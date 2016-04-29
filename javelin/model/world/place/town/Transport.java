package javelin.model.world.place.town;

import javelin.view.screen.town.TransportScreen;

/** Vehicles improve speed / random encounter chance. */
public enum Transport {
	NONE, CARRIAGE, AIRSHIP;

	public double getprice() {
		if (equals(NONE)) {
			return 0;
		}
		if (equals(CARRIAGE)) {
			return 1;
		}
		if (equals(AIRSHIP)) {
			return TransportScreen.COSTAIRSHIP / TransportScreen.COSTCARRIAGE;
		}
		throw new RuntimeException("unknown transport");
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}