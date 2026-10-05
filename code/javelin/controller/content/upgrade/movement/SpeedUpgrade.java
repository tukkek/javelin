package javelin.controller.content.upgrade.movement;

import javelin.controller.challenge.factor.SpeedFactor;
import javelin.controller.content.upgrade.Upgrade;
import javelin.controller.db.reader.fields.Speed;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * Raises a {@link Monster}'s movement speed.
 *
 * @see Monster#walk
 * @see Monster#burrow
 * @see Monster#swim
 * @see Monster#fly
 * @author alex
 */
public abstract class SpeedUpgrade extends Upgrade{
	/** Target speed in feet. */
	protected final int target;

	/** Constructor. */
	public SpeedUpgrade(String name,int target){
		super(name);
		this.target=Math.min(target,Speed.MAXSPEED);
	}

	/**
	 * @return The current speed in feets.
	 */
	protected abstract long getspeed(Monster m);

	/**
	 * @param m Sets {@link #target} to the appropriate {@link Monster} field.
	 */
	protected abstract void setspeed(Monster m);

	@Override
	public String inform(final Combatant m){
		return "Current velocity: "+getspeed(m.source)+" feet";
	}

	@Override
	public boolean apply(final Combatant m){
		if(m.source.fly>0||getspeed(m.source)>=target) return false;
		setspeed(m.source);
		return true;
	}

	int increments(final Monster m,long t){
		int typical=findtypical(m);
		int loops=0;
		while(typical<t){
			typical*=2;
			loops+=1;
		}
		return loops;
	}

	int findtypical(final Monster m){
		return SpeedFactor.TYPICAL_SPEED[m.size];
	}
}