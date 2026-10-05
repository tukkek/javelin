package javelin.controller.content.fight.mutator.mode.haunt;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.content.fight.mutator.mode.Boss;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.generator.encounter.Encounter;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.world.location.haunt.Haunt;

public class HauntBoss extends Boss{
	/**
	 *
	 */
	private final Haunt haunt;

	public HauntBoss(Haunt haunt){
		super(haunt.targetel,null);
		this.haunt=haunt;
	}

	@Override
	protected List<Monster> listbosses(List<Terrain> terrains){
		return haunt.pool;
	}

	@Override
	protected List<Encounter> listminions(List<Terrain> terrains){
		var minions=new ArrayList<Encounter>(haunt.pool.size()*4);
		for(var m:haunt.pool){
			var group=new Combatants(2);
			for(var i=1;i<=8;i++)
				group.add(new Combatant(m,true));
			minions.add(new Encounter(group));
		}
		return minions;
	}
}