package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * Haunt for angels, devas and similarly blessed creatures.
 *
 * @author alex
 */
public class HolyGrounds extends Haunt{
	static final List<String> SUBTYPES=List.of("celestial","good","lawful");
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->m.type.equals(MonsterType.OUTSIDER)||include(m,SUBTYPES))
			.filter(m->m.alignment.isgood()).collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.DESERT,Terrain.HILL,
			Terrain.MOUNTAINS,Terrain.PLAIN);

	/** Constructor. */
	public HolyGrounds(){
		super("Holy grounds",null,POOL,TERRAINS);
	}
}
