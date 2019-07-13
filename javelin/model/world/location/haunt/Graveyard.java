package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.map.location.haunt.GraveyardMap;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

public class Graveyard extends Haunt{
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->m.type.equals(MonsterType.UNDEAD))
			.collect(Collectors.toList());

	/** Constructor. */
	public Graveyard(){
		super("Graveyeard",GraveyardMap.class,POOL);
	}
}
