package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.terrain.Terrain;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * Haunt for evil {@link MonsterType#OUTSIDER}s, demons, devils, etc.
 *
 * @author alex
 */
public class DarkShrine extends Haunt{
	static final List<String> SUBTYPES=List.of("demon (tanar'ri)","devil","evil");
	static final List<Monster> POOL=Monster.MONSTERS
			.stream().filter(m->m.type.equals(MonsterType.OUTSIDER)
					&&m.alignment.isevil()&&include(m,SUBTYPES))
			.collect(Collectors.toList());
	static final List<Terrain> TERRAINS=List.of(Terrain.FOREST,Terrain.MARSH);

	/** Constructor. */
	public DarkShrine(){
		super("Dark shrine",null,POOL,TERRAINS);
	}
}
