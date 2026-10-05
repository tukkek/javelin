package javelin.controller.content.event.wild.positive.skill;

import java.util.List;

import javelin.Javelin;
import javelin.controller.content.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.DisableDevice;
import javelin.model.unit.skill.Knowledge;
import javelin.model.unit.skill.Skill;
import javelin.model.unit.skill.Spellcraft;
import javelin.model.unit.skill.UseMagicDevice;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * Uses {@link Spellcraft}, {@link Knowledge}, {@link UseMagicDevice} or
 * {@link DisableDevice} to acquire a locked treasure.
 *
 * @author alex
 */
public class MysticLock extends SkillEvent{
	static final String PROMPT="You approach a door where you had not expected to find one.\n"
			+"It is covered in faintly glowing symbols and doesn't budge an inch when you try to open it.\n"
			+"Will you try to decipher the symbols? It may take a few hours.\n"
			+"Press s to study the door, or w to walk away...";
	static final List<Skill> SKILLS=List.of(Skill.SPELLCRAFT,Skill.KNOWLEDGE,
			Skill.USEMAGICDEVICE,Skill.DISABLEDEVICE);

	/** Reflection-friendly contructor. */
	public MysticLock(PointOfInterest l){
		super("Mystic lock",l,PROMPT,'s','w');
		remove=false;
	}

	@Override
	public boolean validate(Squad s,int squadel){
		var terrain=Terrain.get(location.x,location.y);
		return terrain.equals(Terrain.DESERT)||terrain.equals(Terrain.FOREST)
				||terrain.equals(Terrain.MOUNTAINS);
	}

	@Override
	protected Combatant getbest(Squad s){
		return s.members.stream().max((a,b)->decipher(a)-decipher(b)).get();
	}

	static int decipher(Combatant c){
		int best=Integer.MIN_VALUE;
		for(var skill:SKILLS)
			best=Math.max(best,c.taketen(skill));
		return best;
	}

	@Override
	protected boolean fumble(Combatant active,PointOfInterest location){
		Javelin.message("The door vanishes completely!",true);
		remove=true;
		return true;
	}

	@Override
	protected boolean attempt(Combatant active){
		Squad.active.delay(RPG.r(1,4));
		return decipher(active)>=dc;
	}

	@Override
	protected String fail(Combatant active){
		return active+" cannot figure the meaning of the inscriptions...";
	}

	@Override
	protected String succeed(Combatant active,String reward){
		return active+" is able to unlock the magic door! Inside he finds: "+reward
				+".";
	}
}
