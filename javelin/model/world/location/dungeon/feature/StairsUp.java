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
	static final String PROMPT="Go up the stairs?";

	/** Cosntructor. */
	public StairsUp(Point p){
		super("dungeonstairsup");
		remove=false;
		enter=true;
	}

	@Override
	public boolean activate(){
		var prompt=prompt()+"\n"
				+"Press ENTER to confirm, any other key to cancel...";
		if(Javelin.prompt(prompt)=='\n'){
			WorldMove.abort=true;
			Dungeon.active.goup();
		}
		return false;
	}

	protected String prompt(){
		return PROMPT;
	}
}
