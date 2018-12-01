package javelin.controller.fight.minigame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.exception.battle.EndBattle;
import javelin.controller.fight.setup.BattleSetup;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Building;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.old.RPG;
import javelin.view.screen.BattleScreen;

/**
 * See minigames.txt
 *
 * @author alex
 */
public class MonsterMadness extends Minigame{
	class Setup extends BattleSetup{
		@Override
		public void place(){
			//
		}

		@Override
		public void setup(){
			state.blueTeam.addAll(generatebuildings());
			generateblue();
			generatered(RPG.rolldice(2,4));
			super.setup();
			float baseap=getaverageap(null);
			spawnblue=baseap+delayblue();
			spawnred=baseap+delayred();
		}
	}

	static final List<Monster> ENEMIES=new ArrayList<>();
	static final List<Encounter> ARMY=new ArrayList<>();

	float spawnblue;
	float spawnred;

	/** Constructor. */
	public MonsterMadness(){
		Organization.ENCOUNTERS.stream().filter(e->e.el>10&&e.group.size()==1)
				.forEach(e->ENEMIES.add(e.group.get(0).source));
		Organization.ENCOUNTERS.stream()
				.filter(e->5<=e.el&&e.el<11&&e.group.size()>=4&&counttypes(e)==1)
				.forEach(e->ARMY.add(e));
		meld=false;
		canflee=false;
		setup=new Setup();
		period=Javelin.PERIODNOON;
	}

	@Override
	public ArrayList<Combatant> getblueteam(){
		return new ArrayList<>(0);
	}

	@Override
	public ArrayList<Combatant> getfoes(Integer teamel){
		return new ArrayList<>(0);
	}

	ArrayList<Combatant> generatered(int amount){
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
		}
		state.redTeam.addAll(red);
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
		});
		state.blueTeam.addAll(blue);
		return blue;
	}

	ArrayList<Combatant> generatebuildings(){
		int nbuildings=RPG.rolldice(5,4);
		ArrayList<Combatant> buildings=new ArrayList<>();
		int width=map.map.length-1;
		int height=map.map[0].length-1;
		for(;nbuildings>0;nbuildings--){
			Building b=new Building("Building","flagpoleblue");
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
		float ap=acting.ap;
		if(ap<spawnblue&&ap<spawnred) return;
		List<Combatant> blue=state.blueTeam.stream().filter(c->!c.source.passive)
				.collect(Collectors.toList());
		int tension=ChallengeCalculator.calculateel(state.redTeam)
				-ChallengeCalculator.calculateel(blue);
		ArrayList<Combatant> reinforcements=null;
		String message=null;
		if(ap>=spawnred&&tension<Difficulty.DIFFICULT){
			message="More enemies arrive!";
			reinforcements=generatered(1);
			spawnred+=delayred();
		}else if(ap>=spawnblue&&tension>Difficulty.DEADLY){
			message="Reinforcements arrive!";
			reinforcements=generateblue();
			spawnblue+=delayblue();
		}
		if(reinforcements==null||reinforcements.isEmpty()) return;
		for(Combatant c:reinforcements)
			c.rollinitiative(ap);
		Javelin.redraw();
		Point p=reinforcements.get(0).getlocation();
		BattleScreen.active.center(p.x,p.y);
		Javelin.message(message+"\n"+Javelin.group(reinforcements),true);
	}

	@Override
	public void checkend(){
		if(state.redTeam.isEmpty()){
			Javelin.message("You have defeated all mosnters! Congratulations!",true);
			throw new EndBattle();
		}
		if(!state.blueTeam.stream().filter(c->c instanceof Building).findAny()
				.isPresent()){
			Javelin.message("You have lost all your buildings :( game over!",true);
			throw new EndBattle();
		}
	}

	float delayred(){
		return RPG.r(0,100)/10f;
	}

	float delayblue(){
		return RPG.r(0,20)/10f;
	}
}
