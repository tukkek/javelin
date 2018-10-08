package javelin.controller.fight.minigame;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.fight.minigame.arena.ArenaFight;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.old.RPG;

/**
 * TODO proper ap as in {@link ArenaFight}
 *
 * TODO win condition
 *
 * @author alex
 */
public class MonsterMadness extends Minigame{
	class Setup extends BattleSetup{
		@Override
		public void rollinitiative(){
			ArrayList<Combatant> buildings=generatebuildings();
			state.blueTeam.addAll(buildings);
			generateblue();
			generatered();
			super.rollinitiative();
			check=state.getcombatants().stream()
					.collect(Collectors.averagingDouble(c->c.ap)).floatValue()
					+RPG.r(0,20)/10f;
		}

		@Override
		public void place(){
			//
		}
	}

	static final List<Monster> ENEMIES=new ArrayList<>();
	static final List<Encounter> ARMY=new ArrayList<>();
	float check;

	public MonsterMadness(){
		Organization.ENCOUNTERS.stream().filter(e->e.el>10&&e.group.size()==1)
				.forEach(e->ENEMIES.add(e.group.get(0).source));
		Organization.ENCOUNTERS.stream()
				.filter(e->5<=e.el&&e.el<11&&e.group.size()>=4&&counttypes(e)==1)
				.forEach(e->ARMY.add(e));
		meld=false;
		canflee=false;
		setup=new Setup();
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		return new ArrayList<>(0);
	}

	@Override
	public ArrayList<Combatant> getblueteam(){
		return new ArrayList<>(0);
	}

	ArrayList<Combatant> generatered(){
		int amount=1;
		while(RPG.chancein(2))
			amount+=1;
		ArrayList<Combatant> red=new ArrayList<>(amount);
		int width=state.map.length-1;
		int height=state.map[0].length-1;
		Point entry=new Point(RPG.r(0,width),RPG.r(0,height));
		if(RPG.chancein(2))
			entry.x=RPG.chancein(2)?0:width;
		else
			entry.y=RPG.chancein(2)?0:height;
		for(;amount>0;amount--){
			Combatant c=new Combatant(RPG.pick(ENEMIES),true);
			Point p=new Point(entry);
			while(!p.validate(0,0,width,height)||isblocked(p.x,p.y))
				p.displace();
			c.setlocation(p);
			red.add(c);
			state.redTeam.add(c);
		}
		return red;
	}

	ArrayList<Combatant> generateblue(){
		List<Combatant> buildings=state.blueTeam.stream()
				.filter(c->c instanceof Building).collect(Collectors.toList());
		if(buildings.isEmpty()) return new ArrayList<>(0);
		Point entry=RPG.pick(buildings).getlocation();
		ArrayList<Combatant> blue=RPG.pick(ARMY).generate();
		blue.forEach(c->{
			Point p=new Point(entry);
			while(!p.validate(0,0,state.map.length,state.map[0].length)
					||isblocked(p.x,p.y))
				p.displace();
			c.setlocation(p);
			state.blueTeam.add(c);
		});
		return blue;
	}

	ArrayList<Combatant> generatebuildings(){
		int nbuildings=RPG.d(5,4);
		ArrayList<Combatant> buildings=new ArrayList<>();
		List<File> avatars=Arrays.asList(new File("avatars").listFiles(
				(dir,name)->name.startsWith("location")&&!name.contains("resource")));
		int width=map.map.length-1;
		int height=map.map[0].length-1;
		for(;nbuildings>0;nbuildings--){
			Building b=new Building("Building",
					RPG.pick(avatars).getName().replaceAll(".png",""));
			b.setlevel(Building.LEVELS[RPG.r(0,3)]);
			buildings.add(b);
			Point p=null;
			while(p==null||isblocked(p.x,p.y))
				p=new Point(RPG.r(0,width),RPG.r(0,height));
			b.setlocation(p);
		}
		return buildings;
	}

	boolean isblocked(int x,int y){
		return map.map[x][y].blocked||state.getcombatant(x,y)!=null;
	}

	int counttypes(Encounter e){
		HashSet<String> count=new HashSet<>(e.size());
		e.group.forEach(c->count.add(c.source.name));
		return count.size();
	}

	@Override
	public void startturn(Combatant acting){
		super.startturn(acting);
		if(acting.ap<check) return;
		check+=RPG.r(0,20)/10f;
		List<Combatant> blue=state.blueTeam.stream().filter(c->!c.source.passive)
				.collect(Collectors.toList());
		int tension=ChallengeCalculator.calculateel(state.redTeam)
				-ChallengeCalculator.calculateel(blue);
		if(tension<Difficulty.DIFFICULT){
			ArrayList<Combatant> enemies=generatered();
			Javelin.redraw();
			Javelin.message("More enemies arrive!\n"+Combatant.group(enemies),true);
		}else if(tension>=Difficulty.DEADLY){
			ArrayList<Combatant> reinforcements=generateblue();
			if(reinforcements.isEmpty()) return;
			Javelin.redraw();
			Javelin.message(
					"Reinforcements arrive!\n"+Combatant.group(reinforcements),true);
		}
	}
}
