package javelin.controller.content.upgrade.movement;

import javelin.model.unit.Monster;

/**
 * Upgrades burrow speed.
 *
 * @see Monster#burrow
 */
public class Burrow extends SpeedUpgrade{
	/** 10 feet upgrade. */
	public static final Burrow BADGER=new Burrow("Burrow: badger",10);

	/** See {@link WalkingSpeed#WalkingSpeed(String, int).} */
	public Burrow(String name,int target){
		super(name,target);
	}

	@Override
	protected long getspeed(Monster m){
		return m.burrow;
	}

	@Override
	protected void setspeed(Monster m){
		m.burrow=target;
	}
}
