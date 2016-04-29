package javelin.model.world.place.town;

import javelin.model.world.Squad;
import javelin.view.screen.Option;

/**
 * Defines the level of comfort a {@link Town} can provide.
 * 
 * @author alex
 */
public enum Accommodations {
	LODGE, HOTEL, HOSPITAL;

	public class RestOption extends Option {
		final public int hours;
		public int periods;

		public RestOption(String name, double price, char c, int hoursp,
				int periodsp) {
			super(name, price, c);
			hours = hoursp;
			periods = periodsp;
		}
	}

	public double getprice() {
		return getquality();
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}

	public Option getrestoption() {
		return new RestOption("Rest at " + this, getfee(), 'r', 8,
				getquality());
	}

	private int getquality() {
		if (this == LODGE) {
			return 1;
		}
		if (this == HOTEL) {
			return 2;
		}
		return 4;
	}

	double getfee() {
		if (this == LODGE) {
			return 0;
		}
		if (this == HOTEL) {
			return Math.max(1, Squad.active.size() * .5);
		}
		return Math.max(1, Squad.active.size() * 2);
	}

	public Option getweekrestoption() {
		return new RestOption("Rest for a week", getfee() * 7 * 2, 'w', 7 * 24,
				7 * getquality());
	}
}
