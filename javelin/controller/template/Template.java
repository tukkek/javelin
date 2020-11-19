package javelin.controller.template;

import java.io.Serializable;
import java.util.List;

import javelin.controller.kit.Kit;
import javelin.controller.upgrade.Upgrade;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.branch.Branch;

/**
 * Applies {@link Upgrade}s to a {@link Combatant}. Simpler than {@link Kit}s,
 * as those upgrades are all applied, with no logic or progression to it.
 *
 * TODO probably doesn't need to be serializable (see {@link Branch}.
 *
 * @author alex
 */
public class Template implements Serializable{
	List<Upgrade> upgrades;

	/** Constructor. */
	protected Template(List<Upgrade> u){
		upgrades=u;
	}

	/** @return How many {@link Upgrade}s were succesful. */
	public int apply(Combatant c,Dungeon d){
		var upgraded=0;
		for(var u:upgrades)
			if(u.upgrade(c)) upgraded+=1;
		return upgraded;
	}

	/** @return {@link Combatant}s {@link Upgrade}d at least once. */
	public int apply(Combatants encounter,Dungeon d){
		var upgraded=0;
		for(var c:encounter)
			if(apply(c,d)>0) upgraded+=1;
		return upgraded;
	}
}