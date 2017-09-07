package javelin.model.transport;

/** Flies overland. */
public class Airship extends Transport {
	/** Constructor. */
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
