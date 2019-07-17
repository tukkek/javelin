package javelin.model.world.location.haunt;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.map.location.haunt.OrcSettlementMap;
import javelin.model.unit.Monster;
import javelin.model.unit.Monster.MonsterType;

/**
 * TODO test and compare to previous pool
 *
 * @author alex
 */
public class OrcSettlement extends Haunt{
	static final List<String> GROUPS=List.of("reptilian","goblinoid","orc");
	static final List<Monster> POOL=new ArrayList<>();

	static{
		for(var m:Monster.MONSTERS)
			if(m.type.equals(MonsterType.HUMANOID)) for(String subtype:m.subtypes)
				if(GROUPS.contains(subtype)){
					POOL.add(m);
					break;
				}
	}

	/** Constructor. */
	public OrcSettlement(){
		super("Orc settlement",OrcSettlementMap.class,POOL);
	}
}
