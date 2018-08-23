package javelin.controller.action.minigame;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.InfiniteList;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.scenario.Campaign;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.view.screen.SquadScreen;

/**
 * Allows access to the Arena at any point in time (unless in battle).
 *
 * @author alex
 */
public class EnterArena implements Runnable{
	@Override
	public void run(){
		Combatants gladiators=new Combatants();
		double crmin=Integer.MIN_VALUE;
		double crmax=SquadScreen.SELECTABLE[SquadScreen.SELECTABLE.length-1];
		float targetel=Campaign.INITIALEL;
		choosegladiators(crmin,crmax,targetel,gladiators);
		throw new StartBattle(new ArenaFight(gladiators));
	}

	String listcurrent(Combatants gladiators){
		if(gladiators.isEmpty()) return "";
		String current="Your current gladiators: ";
		for(Combatant c:gladiators)
			current+=c+" (level "+Math.round(c.source.cr)+"), ";
		return current.substring(0,current.length()-2)+".";
	}

	boolean choosegladiators(double crmin,double crmax,float targetel,
			Combatants gladiators){
		InfiniteList<Monster> candidates=getcandidates(crmin,crmax);
		while(ChallengeCalculator.calculateel(gladiators)<targetel){
			ArrayList<Monster> page=candidates.pop(3);
			ArrayList<String> names=new ArrayList<>(3);
			for(int i=0;i<3;i++){
				Monster m=page.get(i);
				names.add(m+" (level "+Math.round(m.cr)+")");
			}
			String prompt="Select your gladiators:";
			int choice=Javelin.choose(prompt,names,true,false);
			if(choice==-1) return false;
			Monster m=page.get(choice);
			Combatant c=new Combatant(m,true);
			c.maxhp=m.hd.maximize();
			c.hp=c.maxhp;
			gladiators.add(c);
			candidates.remove(m);
		}
		return true;
	}

	InfiniteList<Monster> getcandidates(double crmin,double crmax){
		InfiniteList<Monster> candidates=new InfiniteList<>();
		for(float cr:Javelin.MONSTERSBYCR.keySet())
			if(crmin<=cr&&cr<=crmax) for(Monster m:Javelin.MONSTERSBYCR.get(cr))
				if(!m.internal) candidates.add(m);
		return candidates;
	}
}
