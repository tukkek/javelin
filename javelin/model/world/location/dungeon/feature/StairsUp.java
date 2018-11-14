package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.model.world.location.dungeon.Dungeon;

/**
 * Leaves dungeon or goes to upper floor.
 *
 * @author alex
 */
public class StairsUp extends Feature{
	static final String CONFIRM="Go up the stairs?\nPress ENTER to confirm, any other key to cancel...";

	/** Cosntructor. */
	public StairsUp(String thing,Point p){
		super(p.x,p.y,"dungeonstairsup");
		remove=false;
		enter=true;
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt(CONFIRM)=='\n'){
			WorldMove.abort=true;
			Dungeon.active.goup();
		}
		return false;
	}
}
