package javelin.controller.upgrade.movement;

import javelin.model.unit.Monster;

/**
 * See the d20 SRD for more info.
 */
public class Flying extends WalkingSpeed{
	/** See {@link WalkingSpeed#WalkingSpeed(String, int).} */
	public Flying(String name,int target){
		super(name,target);
	}

	@Override
	protected long getSpeed(Monster m){
		return m.fly;
	}

	@Override
	protected void setSpeed(Monster m){
		m.walk=0;
		m.fly=target;
	}
}
