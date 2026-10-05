package javelin.controller.content.terrain.hazard;

import javelin.controller.content.action.world.WorldMove;
import javelin.controller.content.terrain.Terrain;

/**
 * @see Terrain#gethazards(boolean)
 * @author alex
 */
public abstract class Hazard{

	/**
	 * Called when a hazard happens.
	 *
	 * @param hoursellapsed How many hours passed in the last {@link WorldMove}.
	 */
	public abstract void hazard(int hoursellapsed);

	/**
	 * This is verified before calling {@link #hazard(int)}.
	 *
	 * @return <code>false</code> if the conditions are not right for this hazard
	 *         to happen.
	 */
	public abstract boolean validate();

	@Override
	public boolean equals(Object obj){
		return getClass().equals(obj.getClass());
	}

	@Override
	public int hashCode(){
		return getClass().getCanonicalName().hashCode();
	}
}