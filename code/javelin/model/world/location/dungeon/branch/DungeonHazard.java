package javelin.model.world.location.dungeon.branch;

import java.io.Serializable;

import javelin.controller.content.fight.RandomDungeonEncounter;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.screen.DungeonScreen;

/**
 * A special mechanic that happens while exploring a {@link Dungeon}.
 *
 * TODO if {@link Branch} is not {@link Serializable}, this doesn't need to be
 * either.
 *
 * @see DungeonScreen#explore(int, int)
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