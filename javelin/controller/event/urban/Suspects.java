package javelin.controller.event.urban;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.map.location.TownMap;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.classes.Commoner;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.Trait;
import javelin.old.RPG;

/**
 * {@link Squad} gets arrested by guards. They can fight, bribe or talk their
 * way out of the situation. If they fail, they will spend some time in jail.
 *
 * @author alex
 */
public class Suspects extends UrbanEvent{
	static final String BRIBE="Bribe the guards";
	static final String FIGHT="Fight the guards";
	static final String SURRENDER="Surrender and go to jail until things are sorted out";

	int price=RewardCalculator.getgold(town.population,1);
	List<Monster> guards;

	/** Reflection constructor. */
	public Suspects(Town t){
		super(t,List.of(Trait.CRIMINAL,Trait.MILITARY),Rank.VILLAGE);
		guards=Terrain.get(town.x,town.y).getmonsters().stream()
				.filter(m->m.think(-1)&&m.cr<=el).collect(Collectors.toList());
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return super.validate(s,squadel)&&s!=null&&!guards.isEmpty();
	}

	List<Combatant> generateguards(int rank){
		int nguards=RPG.rolldice(rank,4);
		var guards=new ArrayList<Combatant>(nguards);
		while(guards.size()<nguards&&ChallengeCalculator.calculateel(guards)<el)
			guards.add(new Combatant(RPG.pick(this.guards),notify));
		while(ChallengeCalculator.calculateel(guards)<el)
			Combatant.upgradeweakest(guards,
					List.of(Commoner.SINGLETON,Warrior.SINGLETON));
		return guards;
	}

	@Override
	public void happen(Squad s){
		var diplomat=s.getbest(Skill.DIPLOMACY);
		var diplomacy=diplomat.roll(Skill.DIPLOMACY);
		var message="A squad in "+town
				+" is approached by a group of suspicious guards.";
		if(diplomacy>=10+town.population){
			message+="\nThankfully, "+diplomat
					+" convinces them that you're harmless!";
			Javelin.message(message,true);
			return;
		}
		var rank=town.getrank().rank;
		var guards=generateguards(rank);
		var fight=FIGHT+" ("+Difficulty.describe(s.members,guards)+" fight)";
		var options=new ArrayList<>(List.of(fight,SURRENDER));
		String bribe=null;
		if(s.gold>=price){
			bribe=BRIBE+" ($"+Javelin.format(price)+", you have $"
					+Javelin.format(s.gold)+")";
			options.add(0,bribe);
		}
		var choice=options.get(Javelin.choose(message,options,false,true));
		if(choice==bribe)
			Squad.active.gold-=price;
		else if(choice==fight)
			fight(s,guards);
		else if(choice==SURRENDER)
			surrender(rank);
		else if(Javelin.DEBUG) throw new RuntimeException("Unknown option "+choice);
	}

	void fight(Squad s,List<Combatant> guards){
		EventFight f=new EventFight(guards,s);
		f.map=new TownMap(town);
		f.rewardgold=false;
		throw new StartBattle(f);
	}

	void surrender(int rank){
		var days=RPG.rolldice(rank,8)-rank;
		Squad.active.hourselapsed+=days*24;
		String mesasge="You spend "+days+" days in jail before being cleared.";
		Javelin.message(mesasge,notify);
	}
}
