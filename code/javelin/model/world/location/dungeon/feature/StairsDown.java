package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.content.action.world.WorldMove;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;

/**
 * Descends deeper into the dungeon.
 *
 * @author alex
 */
public class StairsDown extends Feature{
	static final String CONFIRM="Go down the stairs?\nPress ENTER to confirm, any other key to cancel...";

	/** Constructor. */
	public StairsDown(DungeonFloor f){
		super("stairs down");
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
