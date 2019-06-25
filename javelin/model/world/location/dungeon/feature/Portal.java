package javelin.model.world.location.dungeon.feature;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.MagicTemple;
import javelin.old.RPG;

/**
 * @see MagicTemple
 * @author alex
 */
public class Portal extends Feature{
	static final String CONFIRM="Press ENTER to cross the portal or any other key to cancel...";

	enum Destination{
		OUTSIDE,STAIRSUP
	}

	Destination target=RPG.chancein(4)?Destination.OUTSIDE:Destination.STAIRSUP;
	boolean revealed=false;

	/** Constructor. */
	public Portal(){
		super("locationportal");
		remove=false;
	}

	@Override
	public boolean activate(){
		var d=Dungeon.active;
		var known=revealed||Squad.active.getbest(Skill.KNOWLEDGE)
				.taketen(Skill.KNOWLEDGE)>=10+d.level;
		String info;
		if(known){
			var destination=target==Destination.OUTSIDE?"outside"
					:"to the level entrance";
			info="This is a portal leading "+destination+".";
		}else
			info="You are unable to determine the portal's destination...";
		if(Javelin.prompt(info+"\n"+CONFIRM)!='\n') return false;
		revealed=true;
		WorldMove.abort=true;
		if(target==Destination.OUTSIDE)
			d.leave();
		else{
			var stairs=d.features.get(StairsUp.class);
			d.herolocation=new Point(stairs.x-1,stairs.y);
		}
		return true;
	}
}
