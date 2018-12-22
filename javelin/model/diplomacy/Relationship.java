package javelin.model.diplomacy;

import java.io.Serializable;

import javelin.model.unit.Alignment.Ethics;
import javelin.model.unit.Alignment.Morality;
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

	/** If <code>true</code>, town's {@link Ethics} is known. */
	public boolean showethics=false;
	/** If <code>true</code>, town's {@link Morality} is known. */
	public boolean showmorals=false;
	/** A measure of how well the player stands with this faction. */
	public int status=0;
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

	void changestatus(int delta){
		status+=delta;
		if(status>2)
			status=2;
		else if(status<-2) status=-2;
	}

	@Override
	public String toString(){
		return town.toString();
	}

	/**
	 * @return A description of {@link Town#alignment}, taking {@link #showethics}
	 *         and {@link #showmorals} into account.
	 */
	public String describealignment(){
		if(showethics&&showmorals) return town.alignment.toString();
		if(showethics) return town.alignment.ethics+" ???";
		if(showmorals) return "??? "+town.alignment.morals.toString().toLowerCase();
		return "Unknown alignment";
	}

	/**
	 * @return <code>false</code> if {@link Town} hasn't been discovered yet.
	 *         Undiscovered factions are largely ignored by {@link Diplomacy}.
	 */
	public boolean isdiscovered(){
		return WorldScreen.see(town.getlocation());
	}
}