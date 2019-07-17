package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.map.location.haunt.AbandonedManorMap;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * TODO test pool
 *
 * @author alex
 */
public class AbandonedManor extends Haunt{
	static final List<MonsterType> TYPES=List.of(MonsterType.CONSTRUCT,
			MonsterType.VERMIN,MonsterType.PLANT,MonsterType.MAGICALBEAST);
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->!m.passive&&TYPES.contains(m.type))
			.collect(Collectors.toList());

	/** Constructor. */
	public AbandonedManor(){
		super("Abandoned manor",AbandonedManorMap.class,POOL);
	}
}
