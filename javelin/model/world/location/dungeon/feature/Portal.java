package javelin.model.world.location.dungeon.feature;

import java.io.Serializable;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.action.world.WorldMove;
import javelin.controller.generator.dungeon.template.Template;
import javelin.controller.table.dungeon.feature.FeatureModifierTable;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.temple.MagicTemple;
import javelin.old.RPG;

/**
 * Transports you from somewhere to somewhere else in (or out) of a
 * {@link Dungeon}.
 *
 * TODO would be neat to have a portal that transports to another portal (maybe
 * generating a new one).
 *
 * TODO portals leading {@link Destination#OUTSIDE} could also functions as
 * entries, prompted once you enter a {@link Dungeon}. Especially good for
 * deeper dungeons.
 *
 * @see MagicTemple
 * @author alex
 */
public class Portal extends Feature{
	abstract static class Destination implements Serializable{
		String description;

		Destination(String description){
			this.description=description;
		}

		abstract void go();
	}

	static final Destination OUTSIDE=new Destination("outside"){
		@Override
		void go(){
			Dungeon.active.leave();
		}
	};
	static final Destination STAIRSUP=new Destination("to the level entrance"){
		@Override
		void go(){
			var stairs=Dungeon.active.features.get(StairsUp.class);
			var near=RPG.shuffle(stairs.getlocation().getadjacent());
			var to=near.stream().filter(p->validate(p)).findAny().orElse(null);
			if(to!=null)
				Dungeon.active.squadlocation=to;
			else if(Javelin.DEBUG)
				throw new RuntimeException("Cannot teleport to entrance!");
		}

		boolean validate(Point p){
			var d=Dungeon.active;
			return p.validate(0,0,d.size,d.size)&&d.map[p.x][p.y]==Template.FLOOR;
		}
	};
	static final String CONFIRM="Press ENTER to cross the portal or any other key to cancel...";
	static final String UNKNOWN="You are unable to determine the portal's destination...";

	int knowledgedc=10+Dungeon.active.level
			+Dungeon.active.tables.get(FeatureModifierTable.class).rollmodifier();
	Destination destination=RPG.chancein(4)?OUTSIDE:STAIRSUP;
	boolean revealed=false;

	/** Constructor. */
	public Portal(){
		super("locationportal");
		remove=false;
	}

	@Override
	public boolean activate(){
		var known=revealed||Squad.active.getbest(Skill.KNOWLEDGE)
				.taketen(Skill.KNOWLEDGE)>=knowledgedc;
		var info=known?"This is a portal leading "+destination.description+"."
				:UNKNOWN;
		if(Javelin.prompt(info+"\n"+CONFIRM)!='\n') return false;
		revealed=true;
		WorldMove.abort=true;
		destination.go();
		return true;
	}
}
