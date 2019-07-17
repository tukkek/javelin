package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * {@link Haunt} with {@link MonsterType#ELEMENTAL}s and mephits.
 *
 * @author alex
 */
public class Conflux extends Haunt{
	static final List<String> SUBTYPES=List.of("mephit");
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->m.type.equals(MonsterType.ELEMENTAL)||include(m,SUBTYPES))
			.filter(m->m.walk>0||m.fly>0||m.burrow>0).collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.DESERT,Terrain.MOUNTAINS,
			Terrain.FOREST,Terrain.MARSH);

	/** Constructor. */
	public Conflux(){
		super("Elemental conflux",null,POOL,TERRAINS);
	}
}
