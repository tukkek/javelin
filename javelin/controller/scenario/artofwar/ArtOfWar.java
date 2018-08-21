package javelin.controller.scenario.artofwar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.RewardCalculator;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.controller.upgrade.classes.Warrior;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * See scenarios.txt.
 *
 * @author alex
 */
public class ArtOfWar extends Scenario{
	static public ArtOfWar singleton=new ArtOfWar();

	/**
	 * Challenge Rating of the last game-world challenge.
	 *
	 * TODO using a conservative 20 to start, can be raised with playtesting
	 */
	static final int ENDGAME=20;
	static final int COMMANDERCRMIN=6;
	static final int COMMANDERCRMAX=10;

	Terrain region=null;

	private ArtOfWar(){
		statictowns=false;
		featuregenerator=AowGenerator.class;
		helpfile="artofwar";
		labormodifier=0;
		spawn=false;
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
		Squad.active=new Squad(0,0,8,null);
		Combatant commander=selectcommander();
		commander.maxhp=commander.source.hd.average();
		commander.hp=commander.maxhp;
		Squad.active.members.add(commander);
		int gold=RewardCalculator.calculatepcequipment(COMMANDERCRMAX);
		Squad.active.gold=Javelin.round(gold);
	}

	Combatant selectcommander(){
		List<Monster> commanders=sort(filtercommanders());
		int i=Javelin.choose("Select your commander:",commanders,true,false);
		Monster m=i>=0?commanders.get(i):RPG.pick(commanders);
		Combatant commander=new Combatant(m,true);
		commander.source.elite=true;
		commander.xp=new BigDecimal(COMMANDERCRMAX-m.cr);
		return commander;
	}

	List<Monster> filtercommanders(){
		return getunits(region).stream()
				.filter(m->COMMANDERCRMIN<=m.cr&&m.cr<=COMMANDERCRMAX&&m.think(+1))
				.collect(Collectors.toList());
	}

	List<Monster> getunits(Terrain t){
		return Javelin.ALLMONSTERS.stream()
				.filter(m->m.getterrains().contains(t.toString()))
				.collect(Collectors.toList());
	}

	Terrain selectregion(){
		List<Terrain> regions=sort(Arrays.asList(Terrain.NONWATER));
		int i=Javelin.choose("Select your region:",regions,true,false);
		return i>=0?regions.get(i):RPG.pick(regions);
	}

	<K> List<K> sort(List<K> l){
		l.sort((o1,o2)->o1.toString().compareTo(o2.toString()));
		return l;
	}

	@Override
	public void endday(double day){
		if(day%7!=0) return;
		ArrayList<Combatant> eligible=new ArrayList<>();
		for(Squad s:Squad.getsquads())
			for(Combatant c:s.members)
				if(c.xp.floatValue()>=1) eligible.add(c);
		if(eligible.isEmpty()) return;
		int week=Math.round(Math.round(day/7));
		String prompt="Week "+week+": do you want to upgrade your units?\n"
				+"Press u to upgrade or ENTER to cancel...";
		Character confirm=' ';
		while(confirm!='u'&&confirm!='\n')
			confirm=Javelin.prompt(prompt);
		if(confirm=='u') for(Combatant c:eligible)
			while(c.upgrade(Warrior.SINGLETON))
				continue;
	}
}
