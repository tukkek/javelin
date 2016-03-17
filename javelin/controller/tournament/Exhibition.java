package javelin.controller.tournament;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.model.world.Incursion;
import javelin.model.world.town.Town;
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
	public static boolean DEBUG = false;
	public static final ArrayList<Exhibition> SPECIALEVENTS =
			new ArrayList<Exhibition>();

	static {
		SPECIALEVENTS.add(new MirrorMatch());
		SPECIALEVENTS.add(new Champion());
		SPECIALEVENTS.add(new Horde());
		// SPECIALEVENTS.add(new SquadExhibition());
	}

	public String name;

	public Exhibition(String namep) {
		name = namep;
	}

	public static void opentournament() {
		if (!Incursion.squads.isEmpty()) {
			return;
		}
		if (RPG.r(1, 7) == 1 || DEBUG) {
			RPG.pick(javelin.model.world.town.Town.towns).host();
		}
	}

	abstract public void start();
}
