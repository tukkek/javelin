package javelin.controller.scenario.artofwar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.fight.Fight;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.Actor;
import javelin.model.world.World;
import javelin.old.RPG;

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

	int el=INITIALEL;

	/** Constructor. */
	public ArtOfWar(){
		statictowns=false;
		featuregenerator=AowGenerator.class;
		helpfile="Art of war";
		labormodifier=0;
		spawn=false;
		asksquadnames=false;
	}

	@Override
	public boolean win(){
		for(Actor a:World.getactors()){
			WarLocation wl=a instanceof WarLocation?(WarLocation)a:null;
			if(wl!=null&&wl.win&&wl.ishostile()) return false;
		}
		Javelin.message("You have captured the strongest garisson!\n"
				+"Congratulations, you win!",true);
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

	static void reinforce(String title,int targetel,List<Combatants> available){
		var s=Squad.active;
		while(ChallengeCalculator.calculateel(s.members)<targetel){
			var hires=selecthires(available,s,targetel);
			if(hires.isEmpty()) break;
			var options=hires.stream().map(e->Javelin.group(e))
					.collect(Collectors.toList());
			String prompt=title+"\n\nCurrently:\n"
					+Javelin.group(Squad.active.members);
			var choice=Javelin.choose(prompt,options,true,true);
			s.members.addAll(hires.get(choice).generate());
		}
	}

	static List<Combatants> selecthires(List<Combatants> encounters,Squad s,
			int targetel){
		Collections.shuffle(encounters);
		var hires=new ArrayList<Combatants>(NHIRES);
		for(var e:encounters){
			var group=new Combatants(e);
			group.addAll(s.members);
			if(ChallengeCalculator.calculateel(group)<=targetel){
				hires.add(e);
				if(hires.size()==NHIRES) break;
			}
		}
		hires.sort((a,b)->Javelin.group(a).compareTo(Javelin.group(b)));
		return hires;
	}

	static Terrain selectregion(){
		var terrains=Arrays.asList(Terrain.NONWATER);
		terrains.sort((o1,o2)->o1.toString().compareTo(o2.toString()));
		ArrayList<Object> choices=new ArrayList<>(terrains);
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
		if(!victory){
			el-=1;
			reinforce("You have lost reputation...");
			return;
		}
		var elblue=ChallengeCalculator.calculateel(Fight.originalblueteam);
		var elred=ChallengeCalculator.calculateel(Fight.originalredteam);
		if(elred>=elblue){
			el+=1;
			reinforce("You have increased your reputation!");
		}else
			reinforce("Your reputation remains unchanged.");
	}

	void reinforce(String title){
		List<Combatants> available=new ArrayList<>();
		for(Actor a:World.getactors()){
			WarLocation wl=a instanceof WarLocation?(WarLocation)a:null;
			if(wl!=null&&!wl.ishostile()) available.addAll(wl.hires);
		}
		if(!available.isEmpty())
			reinforce(title+"\nSelect reinforcements.",el,available);
	}
}
