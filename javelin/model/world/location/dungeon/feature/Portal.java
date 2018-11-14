package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.MagicTemple;

/**
 * @see MagicTemple
 * @author alex
 */
public class Portal extends Feature{
	private static final String PROMPT="Do you want to enter the portal?\n"
			+"Press enter to cross it, any other key to cancel...";

	/** Constructor. */
	public Portal(){
		super("locationportal");
		remove=false;
	}

	@Override
	public boolean activate(){
		if(Javelin.prompt(PROMPT)!='\n') return true;
		Dungeon d=Dungeon.active;
		StairsUp stairs=d.features.get(StairsUp.class);
		d.herolocation=new Point(stairs.x-1,stairs.y);
		WorldMove.abort=true;
		return true;
	}
}
