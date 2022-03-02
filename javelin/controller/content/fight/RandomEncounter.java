package javelin.controller.content.fight;

import java.util.ArrayList;
import java.util.List;

import javelin.Debug;
import javelin.JavelinApp;
import javelin.controller.challenge.Difficulty;
import javelin.controller.content.action.ai.Flee;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.Season;
import javelin.model.world.World;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * Fight that happens on the overworld map.
 *
 * @author alex
 */
public class RandomEncounter extends Fight{
	static final int ENCOUNTERS=20;
	static final int REFRESHDIE=4;
	static final int REFRESHCHANCE=Math
			.round((1+REFRESHDIE)/2*2*Season.SEASONDURATION/ENCOUNTERS);

	@Override
	public ArrayList<Combatant> generate(){
		var e=RPG.pick(World.seed.encounters.get(Terrain.current()));
		return skip(e)?null:new Combatants(e);
	}

	@Override
	public boolean avoid(List<Combatant> foes){
		return foes==null||super.avoid(foes);
	}

	/**
	 * TODO better strategic skip
	 *
	 * @return <code>true</code> if encounter is too easy to bother the player
	 *         with - skip it.
	 */
	public static boolean skip(Combatants c){
		return Difficulty.calculate(Squad.active.members,c)<=Flee.FLEEAT;
	}

	/**
	 * @param chance % chance of starting a battle.
	 * @throws StartBattle
	 */
	static public void encounter(double chance){
		if(!Debug.disablecombat&&RPG.random()<chance){
			Fight f=JavelinApp.context.encounter();
			if(f!=null) throw new StartBattle(f);
		}
	}

	static void generate(List<Combatants> encounters,int target,Terrain t){
		while(encounters.size()<target){
			var el=Math.min(RPG.r(1,t.tier),RPG.r(1,t.tier));
			var e=EncounterGenerator.generate(el,t);
			if(e!=null) encounters.add(e);
		}
	}

	/** @see World#encounters */
	public static void generate(World w){
		for(var t:Terrain.NONUNDERGROUND){
			var target=RPG.randomize(ENCOUNTERS,1,Integer.MAX_VALUE);
			var encounters=new ArrayList<Combatants>(target);
			generate(encounters,target,t);
			w.encounters.put(t,encounters);
		}
	}

	/**
	 * Modify {@link World#encounters} over time. The goal is to have half the
	 * {@link Encounter}s changed with every {@link Season#SEASONDURATION}.
	 *
	 * Called daily.
	 *
	 * @see WorldScreen
	 */
	public static void evolve(){
		for(var t:Terrain.NONUNDERGROUND){
			if(!RPG.chancein(REFRESHCHANCE)) continue;
			var encounters=World.seed.encounters.get(t);
			var trim=encounters.size()-RPG.r(1,REFRESHDIE);
			if(trim<1) trim=1;
			while(encounters.size()>trim)
				encounters.remove(RPG.r(0,encounters.size()-1));
			generate(encounters,encounters.size()+RPG.r(1,REFRESHDIE),t);
		}
	}
}