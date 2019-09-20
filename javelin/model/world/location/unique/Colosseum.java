package javelin.model.world.location.unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.comparator.CombatantsByNameAndMercenary;
import javelin.controller.exception.GaveUp;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.WavesFight;
import javelin.controller.fight.minigame.arena.Arena;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.map.location.LocationMap;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.view.Images;

/**
 * TODO a replacement for the {@link Arena} minigame and the TrainingHall
 * location. Should probably be named Arena but for now let's not clash names.
 *
 * TODO should be considered a winnable game objective
 *
 * TODO would probably want to up the ante by adding a number of allied buddies
 * up to a given EL
 *
 * @author alex
 */
public class Colosseum extends UniqueLocation{
	static final String CONFIRM="Begin an Arena match with these fighters?\n"
			+"Press ENTER to confirm or any other key to cancel...\n\n";
	static final String DESCRIPTION="The Arena";

	class ColosseumMap extends LocationMap{
		public ColosseumMap(){
			super("colosseum");
			wall=Images.get("terrainorcwall");
			floor=Images.get("terraindesert");
		}
	}

	class ColosseumFight extends WavesFight{
		ArrayList<Combatant> fighters;

		public ColosseumFight(ArrayList<Combatant> fighters){
			super(Colosseum.this,new ColosseumMap(),
					ChallengeCalculator.calculateel(fighters)); //TODO change map name
			friendly=true;
			friendlylevel=Combatant.STATUSINJURED;
			this.fighters=fighters;
		}

		@Override
		public ArrayList<Combatant> getblueteam(){
			return fighters;
		}

		@Override
		protected Combatants generatewave(int el) throws GaveUp{
			//TODO would be cool to have some NPC waves, even if EncounterGenerator should really handle that instead
			return EncounterGenerator.generate(el,Arrays.asList(Terrain.ALL));
		}
	}

	/** Constructor. */
	public Colosseum(){
		super(DESCRIPTION,DESCRIPTION,15,20);
		generategarrison=false;
	}

	@Override
	public List<Combatant> getcombatants(){
		return null;
	}

	Combatant choosefighter(ArrayList<Combatant> fighters){
		var left=Squad.active.members.stream()
				.filter(c->c.getnumericstatus()==Combatant.STATUSUNHARMED)
				.collect(Collectors.toList());
		left.removeAll(fighters);
		if(left.isEmpty()) return null;
		var prompt="Add which fighter to your team?\n\nCurrently selected: ";
		var current=fighters.isEmpty()?"none selected yet":Javelin.group(fighters);
		var choices=left.stream().sorted(CombatantsByNameAndMercenary.SINGLETON)
				.map(c->c+" ("+c.getstatus()+")").collect(Collectors.toList());
		var choice=Javelin.choose(prompt+current+".",choices,true,false);
		if(choice<0) return null;
		return left.get(choice);
	}

	@Override
	public boolean interact(){
		if(!super.interact()) return false;
		var fighters=new ArrayList<Combatant>(Squad.active.members.size());
		Combatant fighter=choosefighter(fighters);
		while(fighter!=null){
			fighters.add(fighter);
			fighter=choosefighter(fighters);
		}
		if(!fighters.isEmpty()
				&&Javelin.prompt(CONFIRM+Javelin.group(fighters)+".")=='\n')
			throw new StartBattle(new ColosseumFight(fighters));
		return false;
	}

	@Override
	public String getimagename(){
		return "locationtraininghall"; //TODO
	}
}
