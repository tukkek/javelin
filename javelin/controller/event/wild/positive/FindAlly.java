package javelin.controller.event.wild.positive;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.challenge.Difficulty;
import javelin.controller.event.wild.Wanderer;
import javelin.controller.terrain.Terrain;
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
	public FindAlly(){
		super("Find ally");
	}

	@Override
	public boolean validate(Squad s,int squadel,PointOfInterest l){
		var terrain=Terrain.get(l.x,l.y);
		var candidates=terrain.getmonsters().stream().filter(m->m.think(-1)).filter(
				m->squadel+Difficulty.IRRELEVANT+1<=m.cr&&m.cr<=squadel+Difficulty.EASY)
				.collect(Collectors.toList());
		if(candidates.isEmpty()) return false;
		ally=RPG.pick(candidates);
		return true;
	}

	@Override
	public void happen(Squad s,PointOfInterest l){
		var prompt="A friendly "+ally.toString().toLowerCase()
				+" wants to join your ranks!";
		var options=List.of("Welcome aboard!","Decline");
		if(Javelin.choose(prompt,options,false,true)==0) s.recruit(ally);
	}
}
