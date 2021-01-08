package javelin.controller.content.upgrade.movement;

import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class WalkingSpeed extends SpeedUpgrade{
	/** 30 feet upgrade. */
	public static final WalkingSpeed HUMAN=new WalkingSpeed("Speed: human",30);
	/** 50 feet upgrade. */
	public static final WalkingSpeed CHEETAH=new WalkingSpeed("Speed: cheetah",
			50);

	public WalkingSpeed(final String name,final int target){
		super(name,target);
	}

	@Override
	protected void setspeed(Monster m){
		m.walk=target;
	}

	@Override
	protected long getspeed(Monster m){
		return m.walk;
	}
}