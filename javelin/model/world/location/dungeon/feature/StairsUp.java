package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.content.action.world.WorldMove;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;

/**
 * Leaves dungeon or goes to upper floor.
 *
 * @author alex
 */
public class StairsUp extends Feature{
	/** Message to show on {@link #activate()}. */
	protected String prompt="Go up the stairs?";

	DungeonFloor floor;

	/** Cosntructor. */
	public StairsUp(Point p,DungeonFloor f){
		super("stairs up");
		remove=false;
		enter=true;
		floor=f;
	}

	@Override
	public boolean activate(){
		var prompt=this.prompt+"\n"
				+"Press ENTER to confirm, any other key to cancel...";
		if(Javelin.prompt(prompt)=='\n'){
			WorldMove.abort=true;
			Dungeon.active.goup();
		}
		return false;
	}

	@Override
	public String toString(){
		return "Stairs to level "+floor.getfloor();
	}
}
