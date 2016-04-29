package javelin.model.world.place.unique;

import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.place.WorldPlace;
import javelin.model.world.place.guarded.GuardedPlace;

/**
 * An unique location is a {@link WorldPlace} that is only featured in a single
 * location in the {@link World} map during a game. They are usually places of
 * high power, which makes them {@link WorldPlace#impermeable} by default.
 * 
 * @author alex
 */
public abstract class UniqueLocation extends GuardedPlace {
	/**
	 * Constructor. See {@link WorldPlace#WorldPlace(String)}.
	 * 
	 * @param minel
	 * @param maxel
	 */
	public UniqueLocation(String descriptionknown, String descriptionunknown,
			int minel, int maxel) {
		super(descriptionknown, descriptionunknown, minel, maxel);
		impermeable = true;
		allowentry = false;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		return Incursion.ignoreincursion(attacker);
	}

	@Override
	protected Integer getel(int attackel) {
		return null;
	}

}