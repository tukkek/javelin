package javelin.controller.scenario.artofwar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.comparator.MonstersByName;
import javelin.controller.fight.Fight;
import javelin.controller.fight.Siege;
import javelin.controller.fight.minigame.battlefield.Reinforcement;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.Upgrade;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;
import javelin.view.screen.SquadScreen;

/**
 * See scenarios.txt.
 *
 * @author alex
 */
public class ArtOfWar extends Scenario{
	/**
	 * Challenge Rating of the last game-world challenge.
	 *
	 * TODO using a conservative 20 to start, can be raised with playtesting
	 */
	static final int ENDGAME=20+4;
	static final int INITIALEL=6+4;
	static final String RANDOMTERRAIN="random";

	static transient int fightel;

	Terrain region=null;

	private ArtOfWar(){
		statictowns=false;
		featuregenerator=AowGenerator.class;
		helpfile="artofwar";
		labormodifier=0;
		spawn=false;
		asksquadnames=false;
	}

	@Override
	public boolean win(){
		for(Town t:Town.gettowns())
			if(t.ishostile()) return false;
		String win="Congratulations, you have won this scenario!\nThanks for playing!";
		Javelin.message(win,true);
		return true;
	}

	@Override
	public void setup(){
		region=selectregion();
		Squad.active=selectarmy();
	}

	Squad selectarmy(){
		List<Monster> units=getunits(region).stream().filter(m->m.cr<=5)
				.collect(Collectors.toList());
		return new SquadScreen(units).open();
	}

	List<Monster> getunits(Terrain t){
		List<Monster> units=Javelin.ALLMONSTERS.stream()
				.filter(m->m.getterrains().contains(t.toString()))
				.collect(Collectors.toList());
		units.sort(MonstersByName.INSTANCE);
		return units;
	}

	Terrain selectregion(){
		List<Terrain> regions=sort(Arrays.asList(Terrain.NONWATER));
		ArrayList<Object> choices=new ArrayList<>(regions);
		choices.add(RANDOMTERRAIN);
		int i=Javelin.choose("Select your region:",choices,true,false);
		if(i==-1) System.exit(0);
		Object choice=choices.get(i);
		return choice==RANDOMTERRAIN?RPG.pick(regions):(Terrain)choice;
	}

	<K> List<K> sort(List<K> l){
		l.sort((o1,o2)->o1.toString().compareTo(o2.toString()));
		return l;
	}

	@Override
	public void start(Fight f,List<Combatant> blue,List<Combatant> red){
		fightel=ChallengeCalculator.calculateel(red);
		f.rewardgold=false;
		f.bribe=false;
		f.hide=false;
	}

	@Override
	public void end(Fight f,boolean victory){
		if(!victory) return;
		levelup();
		reinforce();
		Siege s=(Siege)f;
		s.location.remove();
	}

	void reinforce(){
		fightel=Math.max(1,fightel-4);
		List<Combatants> choices=new Reinforcement(fightel,List.of(region))
				.getchoices();
		String prompt="Select your reinforcements:";
		Combatants choice=choices.get(Javelin.choose(prompt,choices,true,true));
		for(Combatant c:choice)
			c.setmercenary(false);
		Squad.active.members.addAll(choice);
	}

	void levelup(){
		for(Squad s:Squad.getsquads())
			for(Combatant c:s.members)
				if(c.xp.floatValue()>=1) while(c.upgrade(Warrior.SINGLETON))
					continue;
	}

	@Override
	public boolean checkfullsquad(ArrayList<Combatant> squad){
		return ChallengeCalculator.calculateel(squad)>=INITIALEL;
	}

	@Override
	public void upgradesquad(ArrayList<Combatant> squad){
		List<Upgrade> upgrades=List.of(Warrior.SINGLETON);
		while(ChallengeCalculator.calculateel(squad)<INITIALEL)
			Combatant.upgradeweakest(squad,upgrades);
	}
}
