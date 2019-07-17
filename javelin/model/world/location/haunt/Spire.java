package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * Haunt for {@link MonsterType#DRAGON}s.
 *
 * @author alex
 */
public class Spire extends Haunt{
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->m.type.equals(MonsterType.DRAGON))
			.collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.MOUNTAINS);

	/** Constructor. */
	public Spire(){
		super("Dragon spire",null,POOL,TERRAINS);
	}
}
