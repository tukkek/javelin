package javelin.model.world.location.haunt;

import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.map.location.haunt.OrcSettlementMap;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * TODO test and compare to previous pool
 *
 * @author alex
 */
public class OrcSettlement extends Haunt{
	static final List<String> GROUPS=List.of("reptilian,goblinoid,orc");
	static final List<Monster> POOL=Monster.MONSTERS.stream()
			.filter(m->m.type.equals(MonsterType.HUMANOID)
					&&GROUPS.contains(m.group.toLowerCase()))
			.collect(Collectors.toList());

	/** Constructor. */
	public OrcSettlement(){
		super("Orc settlement",OrcSettlementMap.class,POOL);
	}
}
