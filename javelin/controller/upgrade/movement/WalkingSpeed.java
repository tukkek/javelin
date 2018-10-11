package javelin.controller.upgrade.movement;

import javelin.controller.challenge.factor.SpeedFactor;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class WalkingSpeed extends Upgrade{
	protected final int target;

	public WalkingSpeed(final String name,final int target){
		super(name);
		this.target=target;
	}

	@Override
	public String inform(final Combatant m){
		return "Current velocity: "+getSpeed(m.source)+" feet";
	}

	@Override
	public boolean apply(final Combatant m){
		if(m.source.fly>0||getSpeed(m.source)>=target) return false;
		setSpeed(m.source);
		return true;
	}

	protected void setSpeed(Monster m){
		m.walk=target;
	}

	protected long getSpeed(Monster m){
		return m.walk;
	}

	public int increments(final Monster m,long t){
		int typical=findtypical(m);
		int loops=0;
		while(typical<t){
			typical*=2;
			loops+=1;
		}
		return loops;
	}

	public int findtypical(final Monster m){
		return SpeedFactor.TYPICAL_SPEED[m.size];
	}
}