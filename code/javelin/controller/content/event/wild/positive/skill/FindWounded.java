package javelin.controller.content.event.wild.positive.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Heal;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Actor;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * Uses {@link Heal} to treat a wounded creature. May get attacked on a fumble.
 *
 * @author alex
 */
public class FindWounded extends SkillEvent{
	class WoundedFight extends EventFight{
		Combatant wounded=new Combatant(FindWounded.this.wounded,true);

		WoundedFight(List<Combatant> foes,Actor l){
			super(foes,l);
		}

		@Override
		public ArrayList<Combatant> getblueteam(){
			var blue=super.getblueteam();
			wounded.hp=wounded.maxhp/5;
			wounded.automatic=true;
			blue.add(wounded);
			return blue;
		}

		@Override
		public boolean onend(){
			if(state.blueteam.contains(wounded))
				Javelin.message("The wounded "+wounded+" decides to join your party!",
						true);
			else
				Squad.active.remove(wounded);
			return super.onend();
		}
	}

	Monster wounded=null;
	String name=null;

	/** Reflection-friendly operation. */
	public FindWounded(PointOfInterest l){
		super("Find wounded",l,null,'h','w');
	}

	@Override
	public void define(Squad s,int squadel){
		wounded=selectwounded(s,location,0);
		name=wounded.toString().toLowerCase();
		prompt="You find a wounded "+name+".\n"
				+"Do you want to stop and treat the wounded creature?\n"
				+"Press h to heal the "+name+" or w to walk away...";
		super.define(s,squadel);
	}

	/**
	 * @param thinking Minimum intelligence bonus.
	 * @return A single monster of relevant terrain and power level.
	 */
	public static Monster selectwounded(Squad s,PointOfInterest l,int thinking){
		float min=+Float.MAX_VALUE;
		float max=-Float.MAX_VALUE;
		for(var member:s){
			min=Math.min(min,member.source.cr);
			max=Math.max(max,member.source.cr);
		}
		Monster wounded=null;
		while(wounded==null){
			final var min2=min;
			final var max2=max;
			var candidates=Terrain.get(l.x,l.y).getmonsters().stream()
					.filter(m->m.think(thinking)).filter(m->min2<=m.cr&&m.cr<=max2)
					.collect(Collectors.toList());
			if(!candidates.isEmpty()) wounded=RPG.pick(candidates);
			min-=1;
			max+=1;
		}
		return wounded;
	}

	@Override
	protected Combatant getbest(Squad s){
		return s.getbest(Skill.HEAL);
	}

	@Override
	protected boolean attempt(Combatant active){
		return active.roll(Skill.HEAL)>=dc;
	}

	@Override
	protected String fail(Combatant active){
		return "Despite your best efforts, the "+name
				+" doesn't survive for long...";
	}

	@Override
	protected String succeed(Combatant active,String reward){
		return active+" finishes patching up the "+name+"!\n"
				+"In gratitude, the creature reveals a stash hidden prior to being attacked: "
				+reward+".";
	}

	@Override
	protected boolean fumble(Combatant active,PointOfInterest location){
		var foes=EncounterGenerator.generate(
				Math.round(wounded.cr)+Difficulty.get(),
				Terrain.get(location.x,location.y));
		var m="It seems that whoever attacked the "+name
				+" hasn't strayed too far!\n"+"The "+name
				+" feebly rises up in a last attempt to defend itself!";
		Javelin.message(m,true);
		throw new StartBattle(new WoundedFight(foes,location));
	}
}
