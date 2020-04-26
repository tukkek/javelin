package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.Point;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.challenge.Difficulty;
import javelin.controller.comparator.CombatantsByNameAndMercenary;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.Fight;
import javelin.controller.fight.WavesFight;
import javelin.controller.fight.minigame.arena.ArenaMinigame;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.state.Square;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.old.RPG;
import javelin.view.Images;

/**
 * TODO a replacement for the {@link ArenaMinigame} minigame and the
 * TrainingHall location. Should probably be named Arena but for now let's not
 * clash names.
 *
 * TODO should be considered a winnable game objective
 *
 * TODO would probably want to up the ante by adding a number of allied buddies
 * up to a given EL
 *
 * TODO apparently it's failing to generate fights for very low level parties,
 * like going in with a single unit
 *
 * TODO allow escape at any point (even if engaged) - just moving all units to
 * BattleState#fleeing should suffice?
 *
 * @author alex
 */
public class Arena extends UniqueLocation{
	static final String NONEELIGIBLE="Only gladiators in full health are allowed to fight in the arena!";
	static final String CONFIRM="Begin an Arena match with these fighters?\n"
			+"Press ENTER to confirm or any other key to cancel...\n\n";
	static final String DESCRIPTION="The Arena";

	class ColosseumMap extends LocationMap{
		List<Point> minionspawn=new ArrayList<>();

		public ColosseumMap(){
			super("colosseum");
			wall=Images.get("terrainorcwall");
			floor=Images.get("terraindesert");
		}

		@Override
		protected Square processtile(Square s,int x,int y,char c){
			if(c=='3') minionspawn.add(new Point(x,y));
			return super.processtile(s,x,y,c);
		}
	}

	//TODO change map name
	//TODO need to reward gold only after discounting ally's share
	class ColosseumFight extends WavesFight{
		ArrayList<Combatant> fighters;
		int allyel=0;
		int teamel;
		int waveel;

		public ColosseumFight(ArrayList<Combatant> fighters){
			super(Arena.this,new ColosseumMap(),
					ChallengeCalculator.calculateel(fighters));
			friendly=true;
			friendlylevel=Combatant.STATUSINJURED;
			message="New gladiators enter the arena!";
			this.fighters=fighters;
			period=Javelin.PERIODNOON;
			teamel=ChallengeCalculator.calculateel(fighters);
		}

		@Override
		public ArrayList<Combatant> getblueteam(){
			return fighters;
		}

		public void generateallies(){
			if(!RPG.chancein(4)) try{
				//TODO perhaps only intelligent or NPC
				allyel=ChallengeCalculator.calculateel(fighters)
						+RPG.r(Difficulty.EASY,0);
				var allies=EncounterGenerator.generate(allyel,
						Arrays.asList(Terrain.ALL));
				for(var a:allies){
					a.automatic=true;
					a.mercenary=true;
				}
				add(allies,Fight.state.blueTeam,((ColosseumMap)map).minionspawn);
			}catch(GaveUp e){
				allyel=0;
			}
			waveel=ChallengeCalculator.calculateel(Fight.state.blueTeam)
					+getelmodifier();
		}

		@Override
		protected Combatants generatewave(int el) throws GaveUp{
			if(wave==1) generateallies();
			//TODO would be cool to have some NPC waves, even if EncounterGenerator should really handle that instead
			return EncounterGenerator.generate(waveel,Arrays.asList(Terrain.ALL));
		}

		@Override
		protected int getgoldreward(List<Combatant> defeated){
			var gold=super.getgoldreward(defeated);
			return gold*teamel/(allyel+teamel);
		}
	}

	/** Constructor. */
	public Arena(){
		super(DESCRIPTION,DESCRIPTION,15,20);
		generategarrison=false;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	Combatant choosefighter(List<Combatant> squad,List<Combatant> fighters){
		if(squad.isEmpty()) return null;
		var prompt="Add which fighter to your team?\n\nCurrently selected: ";
		var current=fighters.isEmpty()?"none selected yet":Javelin.group(fighters);
		var choices=squad.stream().sorted(CombatantsByNameAndMercenary.SINGLETON)
				.map(c->c+" ("+c.getstatus()+")").collect(Collectors.toList());
		var choice=Javelin.choose(prompt+current+".",choices,true,false);
		return choice>=0?squad.get(choice):null;
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		var hurt=Squad.active.members.stream()
				.filter(c->c.getnumericstatus()<Combatant.STATUSSCRATCHED).limit(1);
		if(hurt.count()>0){
			Javelin.message(NONEELIGIBLE,false);
			return false;
		}
		//		var chosen=new ArrayList<Combatant>(Squad.active.members.size());
		//		Combatant fighter=choosefighter(squad,chosen);
		//		while(fighter!=null){
		//			chosen.add(fighter);
		//			squad.remove(fighter);
		//			fighter=choosefighter(squad,chosen);
		//		}
		//TODO there needs to be a check of whether can generate opponents first, probably be instantiating the Fight first and valitaing
		//TODO use the confirm prompt to pay an entry fee
		var team=new Combatants(Squad.active.members);
		if(Javelin.prompt(CONFIRM+Javelin.group(team)+".")!='\n') return false;
		var f=new ColosseumFight(team);
		/*TODO would be cool to be able to generate fights in advance so here we could check if EmcounterGenerator was able to come up with something or not*/
		throw new StartBattle(f);
	}
}
