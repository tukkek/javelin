package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * Forest grove for beast, magical beasts and feys.
 *
 * @author alex
 */
public class BeastLair extends Haunt{
	static final List<MonsterType> TYPES=List.of(MonsterType.ANIMAL,
			MonsterType.MAGICALBEAST,MonsterType.FEY);
	static final List<String> SUBTYPES=List.of("animal","dire animal");
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->TYPES.contains(m.type)||include(m,SUBTYPES))
			.collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.FOREST);

	/** Constructor. */
	public BeastLair(){
		super("Beast lair",null,POOL,TERRAINS);
	}
}
