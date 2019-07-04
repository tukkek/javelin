package javelin.controller.scenario.artofwar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.fight.Fight;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.old.RPG;
import javelin.view.screen.WorldScreen;

/**
 * See scenarios.txt.
 *
 * @author alex
 */
public class ArtOfWar extends Scenario{
	static final int INITIALEL=6+4;
	/**
	 * Challenge Rating of the last game-world challenge.
	 *
	 * TODO using a conservative 20 to start, can be raised with playtesting
	 */
	static final int ENDGAME=INITIALEL+20;
	static final int NHIRES=5;

	//	int el=INITIALEL;

	List<Combatants> original=new ArrayList<>();

	/** Constructor. */
	public ArtOfWar(){
		//		statictowns=false;
		locationgenerator=AowGenerator.class;
		helpfile="Art of war";
		labormodifier=0;
		spawn=false;
		asksquadnames=false;
	}

	@Override
	public boolean win(){
		for(Actor a:World.getactors()){
			WarLocation wl=a instanceof WarLocation?(WarLocation)a:null;
			if(wl!=null&&wl.town&&wl.realm!=null) return false;
		}
		Javelin.message("You have captured all towns, congratulations!",true);
		return true;
	}

	@Override
	public void setup(){
		var region=selectregion();
		EncounterIndex index=Organization.ENCOUNTERSBYTERRAIN.get(region.name);
		List<Encounter> encounters=new ArrayList<>();
		for(var el:index.keySet())
			if(el<INITIALEL) encounters.addAll(index.get(el));
		int targetel=INITIALEL;
		Squad.active=new Squad(-1,-1,0,null);
		reinforce("Recruit a "+region+" squad.",targetel,encounters.stream()
				.map(e->new Combatants(e.group)).collect(Collectors.toList()));
	}

	void reinforce(String title,int targetel,List<Combatants> available){
		var s=Squad.active;
		while(ChallengeCalculator.calculateel(s.members)<targetel){
			var hires=selecthires(available,s,targetel,NHIRES);
			if(hires.isEmpty()) break;
			var options=hires.stream().map(e->Javelin.group(e))
					.collect(Collectors.toList());
			var random="Random";
			options.add(random);
			var prompt=title+"\n\nCurrently:\n"+Javelin.group(Squad.active.members);
			var choice=Javelin.choose(prompt,options,true,true);
			if(choice==options.size()-1) choice=RPG.r(0,hires.size()-1);
			var reinforcements=hires.get(choice);
			original.add(reinforcements);
			s.members.addAll(reinforcements.generate());
		}
	}

	static List<Combatants> selecthires(List<Combatants> encounters,Squad s,
			int targetel,int max){
		var hires=new ArrayList<Combatants>();
		for(var e:RPG.shuffle(encounters)){
			var group=new Combatants(e);
			group.addAll(s.members);
			if(ChallengeCalculator.calculateel(group)<=targetel){
				hires.add(e);
				if(hires.size()==max) break;
			}
		}
		hires.sort((a,b)->Javelin.group(a).compareTo(Javelin.group(b)));
		return hires;
	}

	static Terrain selectregion(){
		var terrains=new ArrayList<>(Terrain.NONWATER);
		terrains.sort((o1,o2)->o1.toString().compareTo(o2.toString()));
		var choices=new ArrayList<Object>(terrains);
		var random="random";
		choices.add(random);
		int i=Javelin.choose("Select your region:",choices,true,false);
		if(i==-1) System.exit(0);
		Object choice=choices.get(i);
		return choice==random?RPG.pick(terrains):(Terrain)choice;
	}

	@Override
	public void start(Fight f,List<Combatant> blue,List<Combatant> red){
		f.rewardgold=false;
		f.rewardxp=false;
		f.bribe=false;
		f.hide=false;
	}

	@Override
	public void end(Fight f,boolean victory){
		//		heal();
		//		Squad.active.hourselapsed+=24;

		//		if(!victory){
		//			el-=1;
		//			reinforce("You have lost reputation...");
		//			return;
		//		}
		//		var elblue=ChallengeCalculator.calculateel(Fight.originalblueteam);
		//		var elred=ChallengeCalculator.calculateel(Fight.originalredteam);
		//		if(elred>=elblue){
		//			el+=1;
		//			reinforce("You have increased your reputation!");
		//		}else
		//			reinforce("Your reputation remains unchanged.");
	}

	//	void reinforce(String title){
	//		var available=new ArrayList<List<Combatants>>();
	//		available.add(original);
	//		for(var actor:World.getactors()){
	//			var location=actor instanceof WarLocation?(WarLocation)actor:null;
	//			if(location!=null&&!location.ishostile()) available.add(location.hires);
	//		}
	//		var options=new ArrayList<Combatants>(available.size());
	//		for(var hires:available){
	//			var squad=selecthires(hires,Squad.active,el,1);
	//			if(!squad.isEmpty()) options.add(squad.get(0));
	//		}
	//		if(!options.isEmpty())
	//			reinforce(title+"\nSelect reinforcements.",el,options);
	//	}

	@Override
	public void endday(){
		heal();
		super.endday();
		var actors=World.getactors();
		var armies=actors.stream().filter(a->a instanceof Army).map(a->(Army)a)
				.collect(Collectors.toList());
		for(var a:armies)
			move(a,actors);
		if(WorldScreen.lastday<7||WorldScreen.lastday%7!=0) return;
		reinforceplayer(actors);
		var towns=actors.stream().filter(a->a instanceof WarLocation)
				.map(a->(WarLocation)a).filter(wl->wl.town&&wl.realm!=null)
				.collect(Collectors.toList());
		for(var town:towns)
			reinforceai(actors,town);
	}

	/**
	 *
	 */
	protected void heal(){
		for(var squad:Squad.getsquads())
			for(var unit:squad)
				Fountain.heal(unit);
	}

	void reinforceplayer(ArrayList<Actor> actors){
		var all=actors.stream().filter(a->a instanceof WarLocation)
				.map(a->(WarLocation)a).collect(Collectors.toList());
		var captured=all.stream().filter(wl->wl.realm==null&&wl.canhire)
				.sorted((a,b)->Float.compare(a.el,b.el)).collect(Collectors.toList());
		all.forEach(wl->wl.canhire=true);
		if(captured.isEmpty()) return;
		var from=captured.get(captured.size()-1).getlocation();
		while(!World.validatecoordinate(from.x,from.y)
				||World.get(from.x,from.y,actors)!=null
				||Terrain.get(from.x,from.y).equals(Terrain.WATER))
			from.displace();
		var s=new Squad(from.x,from.y,Squad.active.gettime(),null);
		s.place();
		for(var c:captured){
			var hires=new ArrayList<>(c.hires);
			var npc=generatenpc(c.hires);
			if(npc!=null) hires.add(npc);
			String prompt="Choose your weekly reinforcements:";
			var choice=Javelin.choose(prompt,hires,true,true);
			s.members.addAll(hires.get(choice).generate());
		}
		all.forEach(wl->wl.canhire=true);
	}

	Combatants generatenpc(List<Combatants> hires){
		var monsters=new HashSet<Monster>();
		for(var h:hires)
			for(var unit:h)
				monsters.add(unit.source);
		var npc=RPG.pick(monsters);
		var el=ChallengeCalculator.calculateel(RPG.pick(hires));
		var targetcr=ChallengeCalculator.eltocr(el);
		if(npc.cr>=targetcr) return null;
		return new Combatants(List.of(NpcGenerator.generatenpc(npc,targetcr))){
			@Override
			public Combatants generate(){
				return this; //no need to generate new Combatant instances
			}
		};
	}

	void reinforceai(ArrayList<Actor> actors,WarLocation town){
		var captured=actors.stream()
				.filter(a->a instanceof WarLocation&&a.realm==town.realm)
				.map(a->(WarLocation)a).collect(Collectors.toList());
		List<Combatant> reinforcements=new ArrayList<>();
		for(var c:captured)
			reinforcements.addAll(RPG.pick(c.hires).generate());
		var army=new Army(town.x,town.y,reinforcements,town.realm);
		while(!World.validatecoordinate(army.x,army.y)
				||World.get(army.x,army.y,actors)!=null)
			army.displace();
		army.place();
	}

	void move(Army army,ArrayList<Actor> actors){
		var target=actors.stream()
				.filter(a->a instanceof WarLocation&&a.realm!=army.realm
						&&army.getel()>=a.getel(null))
				.map(a->(WarLocation)a).sorted((a,b)->a.distanceinsteps(army.x,army.y)
						-b.distanceinsteps(army.x,army.y))
				.findFirst().orElse(null);
		if(target==null) return;
		if(target.distanceinsteps(army.x,army.y)==1){
			target.realm=army.realm;
			target.garrison.clear();
			target.garrison.addAll(army.squad);
			army.remove();
		}
		if(target.x>army.x)
			army.x+=1;
		else if(target.x<army.x) army.x-=1;
		if(target.y>army.y)
			army.y+=1;
		else if(target.y<army.y) army.y-=1;
	}
}
