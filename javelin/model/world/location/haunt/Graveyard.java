package javelin.model.world.location.haunt;

import java.util.stream.Collectors;

import javelin.controller.map.location.haunt.GraveyardMap;
import javelin.model.unit.Monster;

public class Graveyard extends NewHaunt{

	public Graveyard(){
		super("Graveyeard",GraveyardMap.class,
				Monster.MONSTERS.stream()
						.filter(m->m.type.equals(Monster.MonsterType.UNDEAD))
						.collect(Collectors.toList()));
	}
}
