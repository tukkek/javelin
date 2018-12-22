package javelin.model.diplomacy;

import java.io.Serializable;

import javelin.model.world.location.town.Town;
import javelin.view.screen.WorldScreen;

/**
 * Represents the player's relationship to a given {@link Town}.
 *
 * @author alex
 */
public class Relationship implements Serializable{
	/** Most positive relationship {@link #status}. */
	public static final int ALLY=2;
	/** Positive relationship {@link #status}. */
	public static final int FRIENDLY=1;
	/** Neutral relationship {@link #status}. */
	public static final int INDIFFERENT=0;
	/** Negative relationship {@link #status}. */
	public static final int RESERVED=-1;
	/** Most negative relationship {@link #status}. */
	public static final int HOSTILE=-2;

	/** If <code>true</code>, show {@link Town#alignment}. */
	public boolean showalignment=false;
	/**
	 * A measure of how well the player stands with this faction.
	 *
	 * @see #changestatus(int)
	 * @see #getstatus()
	 */
	int status=0;
	/** Faction in question. */
	public Town town;

	/** Constructor. */
	public Relationship(Town town){
		this.town=town;
	}

	/** @return Description of {@link #status}. */
	public String describestatus(){
		if(status==ALLY) return "Ally";
		if(status==FRIENDLY) return "Friendly";
		if(status==INDIFFERENT) return "Indifferent";
		if(status==RESERVED) return "Reserved";
		if(status==HOSTILE) return "Hostile";
		throw new RuntimeException("Out-of-bounds status #diplomacy: "+status);
	}

	/**
	 * @param delta Applies bonus or penalty to status.
	 */
	public void changestatus(int delta){
		status+=delta;
		if(status>=ALLY){
			status=ALLY;
			showalignment=true;
		}else if(status<HOSTILE) status=HOSTILE;
	}

	/**
	 * @return Current diplomatic status between player and faction, from
	 *         {@link #HOSTILE} to {@link #ALLY}.
	 */
	public int getstatus(){
		return status;
	}

	@Override
	public String toString(){
		return town.toString();
	}

	/**
	 * @return A description of {@link Town#alignment}, taking
	 *         {@link #showalignment} and {@link #showmorals} into account.
	 */
	public String describealignment(){
		return showalignment?town.alignment.toString():"Unknown alignment";
	}

	/**
	 * @return <code>false</code> if {@link Town} hasn't been discovered yet.
	 *         Undiscovered factions are largely ignored by {@link Diplomacy}.
	 */
	public boolean isdiscovered(){
		return WorldScreen.see(town.getlocation());
	}
}