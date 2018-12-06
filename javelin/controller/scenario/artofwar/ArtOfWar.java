package javelin.controller.scenario.artofwar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.db.EncounterIndex;
import javelin.controller.db.reader.fields.Organization;
import javelin.controller.fight.Fight;
import javelin.controller.fight.Siege;
import javelin.controller.generator.encounter.Encounter;
import javelin.controller.scenario.Scenario;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.haunt.Haunt;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

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
	static final int NHIRES=5;

	/** Constructor. */
	public ArtOfWar(){
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
		String win="Congratulations, you have won this scenario!\n"
				+"Thanks for playing!";
		Javelin.message(win,true);
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
		reinforce("Recruit a "+region+" squad.",targetel,encounters);
	}

	static void reinforce(String title,int targetel,List<Encounter> encounters){
		var s=Squad.active;
		while(ChallengeCalculator.calculateel(s.members)<targetel){
			var hires=selecthires(encounters,s,targetel);
			if(hires.isEmpty()) break;
			var options=hires.stream().map(e->Javelin.group(e.group))
					.collect(Collectors.toList());
			String prompt=title+"\n\nCurrently:\n"
					+Javelin.group(Squad.active.members);
			var choice=Javelin.choose(prompt,options,true,true);
			s.members.addAll(hires.get(choice).generate());
		}
	}

	static List<Encounter> selecthires(List<Encounter> encounters,Squad s,
			int targetel){
		Collections.shuffle(encounters);
		var hires=new ArrayList<Encounter>(NHIRES);
		for(var e:encounters){
			var group=new Combatants(e.group);
			group.addAll(s.members);
			if(ChallengeCalculator.calculateel(group)<targetel){
				hires.add(e);
				if(hires.size()==NHIRES) break;
			}
		}
		hires.sort((a,b)->Javelin.group(a.group).compareTo(Javelin.group(b.group)));
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
		if(!victory) return;
		Location l=((Siege)f).location;
		l.remove();
		var targetel=World.getactors().stream().filter(a->a instanceof Location)
				.map(a->ChallengeCalculator.calculateel(((Location)a).garrison))
				.min(Comparator.naturalOrder()).orElse(null);
		if(targetel==null) return;
		var ishaunt=l instanceof Haunt;
		if(!ishaunt) targetel=Math.max(targetel,Squad.active.getel()+RPG.r(1,4));
		List<Encounter> reinforcements;
		if(ishaunt)
			reinforcements=((Haunt)l).dwellers.stream()
					.map(m->new Encounter(List.of(new Combatant(m,true))))
					.collect(Collectors.toList());
		else{
			reinforcements=new ArrayList<>();
			var terrain=Terrain.get(l.x,l.y);
			var index=Organization.ENCOUNTERSBYTERRAIN.get(terrain.name);
			for(int el:index.keySet())
				if(el<targetel) reinforcements.addAll(index.get(el));
		}
		reinforce("Select reinforcements: ",targetel,reinforcements);
	}
}
