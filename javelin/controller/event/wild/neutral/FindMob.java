package javelin.controller.event.wild.neutral;

import java.util.ArrayList;
import java.util.Set;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.event.wild.positive.skill.FindWounded;
import javelin.controller.event.wild.positive.skill.SkillEvent;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.Period;
import javelin.model.world.Season;
import javelin.model.world.location.PointOfInterest;

/**
 * The {@link Squad} has to convince a mob to let go of a victim - getting a
 * reward if succesful. However, in case of a
 * {@link #fumble(Combatant, PointOfInterest)}, they'll have a very tough
 * {@link Fight} ahead of them.
 *
 * @author alex
 */
public class FindMob extends SkillEvent{
	class MobFight extends EventFight{
		Combatant ally;

		MobFight(){
			super(location);
		}

		@Override
		public ArrayList<Combatant> getblueteam(){
			var blue=super.getblueteam();
			ally=new Combatant(victim,true);
			ally.automatic=true;
			blue.add(ally);
			return blue;
		}

		@Override
		public ArrayList<Combatant> getfoes(Integer teamel){
			return mob;
		}

		@Override
		public boolean onend(){
			if(state.blueTeam.contains(ally))
				Javelin.message(ally+" joins your team!",true);
			else
				Squad.active.remove(ally);
			return super.onend();
		}
	}

	Combatants mob=new Combatants();
	Terrain terrain;
	Monster victim;
	private String victimname;

	/** Reflection-friendly constructor. */
	public FindMob(PointOfInterest l){
		super("Find mob",l,null,'t','w');
	}

	@Override
	public boolean validate(Squad s,int squadel){
		if(Season.current==Season.WINTER) return false;
		if(!Period.AFTERNOON.is()&&!Period.EVENING.is()) return false;
		terrain=Terrain.get(location.x,location.y);
		return terrain.equals(Terrain.HILL)||terrain.equals(Terrain.PLAIN);
	}

	@Override
	public void define(Squad s,int squadel){
		victim=FindWounded.selectwounded(s,location,-1);
		while(mob.isEmpty()||ChallengeCalculator.calculateel(mob)<squadel)
			mob.add(new Combatant(victim,true));
		victimname=victim.toString().toLowerCase();
		prompt="You see an intimidating mob cornering a lone "+victimname+".\n"
				+"The mob is composed of "+mob.size()+" "+victimname+".\n"
				+"Do you want to try to calm the mob down?\n"
				+"Press t to talk the mob down or w to walk away from it...";
		super.define(s,squadel);
	}

	@Override
	protected Combatant getbest(Squad s){
		return s.getbest(Skill.DIPLOMACY);
	}

	@Override
	protected boolean fumble(Combatant active,PointOfInterest location){
		var message=active+" tries to gently defuse the situation.\n"
				+"All it seems to achieve though is to make the mob even angrier...";
		Javelin.message(message,true);
		throw new StartBattle(new MobFight());
	}

	@Override
	protected boolean attempt(Combatant active){
		return active.roll(Skill.DIPLOMACY)>=dc;
	}

	@Override
	protected String fail(Combatant active){
		if(Javelin.prompt("The mob doesn't listen to "+active+".\n"
				+"Do you want to attack the mob?\n"
				+"Press a to attack or s to stand by...",Set.of('a','s'))=='a')
			throw new StartBattle(new MobFight());
		return "The mob walks away with the "+victimname+"...";
	}

	@Override
	protected String succeed(Combatant active,String reward){
		return active
				+" succeeds in calming the mob down and they leave their victim alone.\n"
				+"The terrified "+victimname
				+" offers you a gift as a show of gratitude: "+reward+".";
	}
}
