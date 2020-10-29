package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.action.world.WorldMove;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Descends deeper into the dungeon.
 *
 * @author alex
 */
public class StairsDown extends Feature{
	static final String CONFIRM="Go down the stairs?\nPress ENTER to confirm, any other key to cancel...";

	/** Constructor. */
	public StairsDown(){
		super("dungeonstairsdown","stairs down");
		remove=false;
		enter=true;
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt(CONFIRM)=='\n'){
			WorldMove.abort=true;
			Dungeon.active.godown();
		}
		return false;
	}
}
