package javelin.model.unit.transport;

/** Flies overland. */
public class Airship extends Transport {
	public Airship() {
		super("Airship", 100, 100, 16, 50000,
				(50000 - Ship.PRICE) / Carriage.PRICE);
		flies = true;
	}

	@Override
	public boolean battle() {
		return false;
	}
}
