package javelin.controller.upgrade.movement;

import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class Flying extends SpeedUpgrade{
	/** 40 feet upgrade. */
	public static final Flying RAVEN=new Flying("Flying: raven",40);

	/** @see WalkingSpeed#WalkingSpeed(String, int) */
	public Flying(String name,int target){
		super(name,target);
	}

	@Override
	protected long getspeed(Monster m){
		return m.fly;
	}

	@Override
	protected void setspeed(Monster m){
		m.walk=0;
		m.fly=target;
	}
}
