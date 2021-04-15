package javelin.controller.content.event.urban.negative;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.content.event.urban.UrbanEvent;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.generator.encounter.EncounterGenerator;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.unit.Squad;
import javelin.model.world.location.Location;
import javelin.model.world.location.town.Rank;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.diplomacy.Diplomacy;
import javelin.old.RPG;

/**
 * A {@link Town#population} is so livid that they retake their home!
 *
 * TODO should only generate {@link Monster#think(int)} intelligent encounters
 * (at least 1 leader).
 *
 * @author alex
 * @see Diplomacy#REVOLTING
 */
public class Revolt extends UrbanEvent{
	List<Location> targets=town.getdistrict().getlocations().stream()
			.filter(l->!l.impermeable&&!l.sacrificeable&&!l.ishostile())
			.collect(Collectors.toList());
	Combatants mob;

	/** Reflection constructor. */
	public Revolt(Town t){
		super(t,null,Rank.HAMLET);
		mob=EncounterGenerator.generate(el,Terrain.get(town.x,town.y));
	}

	@Override
	public boolean validate(Squad s,int squadel){
		return town.diplomacy.reputation<0&&!targets.isEmpty()&&mob!=null
				&&super.validate(s,squadel);
	}

	@Override
	public void happen(Squad s){
		var target=RPG.pick(targets);
		target.garrison.addAll(mob);
		notify(
				"A group of revolting inhabitants take over "+target+" in "+town+"!");
	}
}
