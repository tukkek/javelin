package javelin.controller.fight.tournament;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.model.world.location.Location;
import javelin.model.world.location.town.Town;
import tyrant.mikera.engine.RPG;

/**
 * One of the events in a tournament.
 * 
 * The EL of any tournament fight should be roughly equal to that of the active
 * squad. Fights are not supposed to be fatal though accidents can happen.
 * 
 * @see Town#ishosting()
 * 
 * @author alex
 */
public abstract class Exhibition implements Serializable {
	/** If <code>true</code> tournaments happen more often. */
	public static boolean DEBUG = false;
	/** Holder for types other than {@link Match}. */
	public static final ArrayList<Exhibition> SPECIALEVENTS =
			new ArrayList<Exhibition>();

	static {
		SPECIALEVENTS.add(new MirrorMatch());
		SPECIALEVENTS.add(new Champion());
		SPECIALEVENTS.add(new Horde());
	}

	/** Text description of this type of fight. */
	public String name;

	/** Constructor. */
	public Exhibition(String namep) {
		name = namep;
	}

	/**
	 * A chance per week that a random {@link Town} will host a tournament.
	 * 
	 * @see Town#host()
	 */
	public static void opentournament() {
		if (RPG.r(1, 7) == 1 || DEBUG) {
			final Town t = (Town) RPG.pick(Location.getall(Town.class));
			t.host();
		}
	}

	/** Deals with the specifics of this exhibition type. */
	abstract public void start();
}
