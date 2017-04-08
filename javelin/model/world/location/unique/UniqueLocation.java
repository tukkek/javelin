package javelin.model.world.location.unique;

import javelin.model.world.Incursion;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.fortification.Fortification;

/**
 * An unique location is a {@link Location} that is only featured in a single
 * location in the {@link World} map during a game. They are usually places of
 * high power, which makes them {@link Location#impermeable} by default.
 *
 * @author alex
 */
public abstract class UniqueLocation extends Fortification {
	/**
	 * Constructor. See {@link Location#WorldPlace(String)}.
	 *
	 * @param minel
	 * @param maxel
	 */
	public UniqueLocation(String descriptionknown, String descriptionunknown,
			int minel, int maxel) {
		super(descriptionknown, descriptionunknown, minel, maxel);
		init(this);
	}

	/**
	 * @param l
	 *            Sets flags common to {@link UniqueLocation}s.
	 */
	public static void init(Location l) {
		l.impermeable = true;
		l.allowentry = false;
		l.realm = null;
		l.gossip = true;
		l.vision = 0;
	}

	public static void makecommon(Fortification l, int minlevel, int maxlevel) {
		l.impermeable = false;
		l.minlevel = minlevel;
		l.maxlevel = maxlevel;
	}

	@Override
	public Boolean destroy(Incursion attacker) {
		return impermeable ? Incursion.ignoreincursion(attacker)
				: super.destroy(attacker);
	}

	@Override
	protected Integer getel(int attackel) {
		return impermeable ? null : super.getel(attackel);
	}
}