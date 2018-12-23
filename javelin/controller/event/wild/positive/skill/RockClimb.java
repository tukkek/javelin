package javelin.controller.event.wild.positive.skill;

import javelin.Javelin;
import javelin.controller.terrain.Mountains;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Acrobatics;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * A {@link Mountains} hidden treasure that requires {@link Acrobatics} or
 * climbing.
 *
 * TODO climbing
 *
 * @author alex
 */
public class RockClimb extends SkillEvent{
	static final String PROMPT="You see in the distance a cave high above in the mountain.\n"
			+"Upon closer inspection, you find a worn out pathway with a brittle rope leading upwards.\n"
			+"Apparently, no one has used the path in ages and it has fallen to disrepair.\n"
			+"Press c to try to climb or w to walk away from it...";

	/** Reflection-friendly constructor. */
	public RockClimb(PointOfInterest l){
		super("Rock climb",l,PROMPT,'c','w');
		remove=false;
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return Terrain.get(location.x,location.y).equals(Terrain.MOUNTAINS);
	}

	@Override
	protected Combatant getbest(Squad s){
		return s.getbest(Skill.ACROBATICS);
	}

	@Override
	protected boolean fumble(Combatant active,PointOfInterest location){
		Javelin.message(active+" falls down in the middle of the climb!",true);
		active.damage(RPG.r(1,(dc-10)*2),active.source.dr);
		return true;
	}

	@Override
	protected boolean attempt(Combatant active){
		return active.taketen(Skill.ACROBATICS)>=dc;
	}

	@Override
	protected String fail(Combatant active){
		return active+" couldn't make it to the top...";
	}

	@Override
	protected String succeed(Combatant active,String reward){
		return active+" climbs through!\n"+"Up there, there is a cache containing: "
				+reward+'.';
	}
}
