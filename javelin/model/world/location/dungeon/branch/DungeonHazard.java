package javelin.model.world.location.dungeon.branch;

import java.io.Serializable;

import javelin.controller.fight.RandomDungeonEncounter;

/**
 * TODO if {@link Branch} is not {@link Serializable}, this doesn't need to be
 * either.
 *
 * @author alex
 */
public abstract class DungeonHazard implements Serializable{
	/**
	 * 2 = 200% likelihood of a {@link RandomDungeonEncounter}, 0.1 = 10%
	 * likelihood...
	 */
	public double chancemodifier=1;

	/**
	 * Called when the hazard does occur.
	 *
	 * @return <code>true</code> if something happened, <code>false</code> to
	 *         ignore.
	 */
	abstract public boolean trigger();
}