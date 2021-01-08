package javelin.controller.content.event.wild.positive;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.content.event.wild.Wanderer;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.exception.battle.StartBattle;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.PointOfInterest;
import javelin.old.RPG;

/**
 * An {@link Difficulty#EASY} combatant offers to join the team.
 *
 * @author alex
 */
public class FindAlly extends Wanderer{
	Monster ally;

	/** Reflection-friendly constructor. */
	public FindAlly(PointOfInterest l){
		super("Find ally",l);
	}

	@Override
	public boolean validate(Squad s,int squadel){
		var terrain=Terrain.get(location.x,location.y);
		var candidates=terrain.getmonsters().stream().filter(m->m.think(-1)).filter(
				m->squadel+Difficulty.IRRELEVANT+1<=m.cr&&m.cr<=squadel+Difficulty.EASY)
				.collect(Collectors.toList());
		if(candidates.isEmpty()) return false;
		ally=RPG.pick(candidates);
		return true;
	}

	@Override
	public void happen(Squad s){
		var prompt="A friendly "+ally.toString().toLowerCase()
				+" wants to join your ranks!";
		var options=List.of("Welcome aboard!","Decline","Attack");
		int choice=Javelin.choose(prompt,options,false,true);
		if(choice==0)
			s.recruit(ally);
		else if(choice==2)
			throw new StartBattle(new EventFight(new Combatant(ally,true),location));
	}
}
