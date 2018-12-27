package javelin.controller.event.urban.negative;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.event.urban.UrbanEvent;
import javelin.controller.exception.GaveUp;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatants;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.old.RPG;

/**
 * A {@link Town#population} is so livid that they retake their home!
 *
 * @author alex
 * @see Town#REVOLTING
 */
public class Revolt extends UrbanEvent{
	List<Location> targets=town.getdistrict().getlocations().stream()
			.filter(l->!l.impermeable&&!l.sacrificeable&&!l.ishostile())
			.collect(Collectors.toList());
	Combatants mob;

	/** Reflection constructor. */
	public Revolt(Town t){
		super(t,null,Rank.HAMLET);
		try{
			mob=EncounterGenerator.generate(el,Terrain.get(town.x,town.y));
		}catch(GaveUp e){
			mob=null;
		}
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return town.describehappiness()==Town.REVOLTING&&!targets.isEmpty()
				&&mob!=null&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var target=RPG.pick(targets);
		target.garrison.addAll(mob);
		if(notify) Javelin.message(
				"A group of revolting inhabitants take over "+target+" in "+town+"!",
				true);
	}
}
