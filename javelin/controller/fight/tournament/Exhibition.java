package javelin.controller.fight.tournament;

import java.io.Serializable;
import java.util.ArrayList;

import javelin.model.world.location.town.Town;

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
public abstract class Exhibition implements Serializable{
	/** If <code>true</code> tournaments happen more often. */
	public static boolean DEBUG=false;
	/** Holder for types other than {@link Match}. */
	public static final ArrayList<Exhibition> SPECIALEVENTS=new ArrayList<>();

	static{
		SPECIALEVENTS.add(new MirrorMatch());
		SPECIALEVENTS.add(new Champion());
		SPECIALEVENTS.add(new Horde());
	}

	/** Text description of this type of fight. */
	public String name;

	/** Constructor. */
	public Exhibition(String namep){
		name=namep;
	}

	/** Deals with the specifics of this exhibition type. */
	abstract public void start();
}
