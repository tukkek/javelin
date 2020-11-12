package javelin.controller.template;

import javelin.controller.kit.Kit;
import javelin.model.unit.Combatant;
import javelin.model.unit.Monster;
import javelin.old.RPG;

/**
 * Applies one of their preferred {@link Kit}s to each {@link Combatant}.
 *
 * @see Kit#getpreferred(javelin.model.unit.Monster, boolean)
 * @author alex
 */
public class KitTemplate extends Template{
	int levels;

	/**
	 * @param levels How many {@link Kit#getupgrades()}-worth of levels to give
	 *          each {@link Combatant}.
	 *
	 * @see Monster#cr
	 */
	public KitTemplate(int levels){
		super(null);
		this.levels=levels;
	}

	@Override
	public int apply(Combatant c){
		var kits=Kit.getpreferred(c.source,true);
		var k=RPG.pick(kits);
		var target=Math.min(c.source.cr+levels,c.source.cr*2);
		var upgrades=0;
		while(c.source.cr<target)
			if(k.upgrade(c))
				upgrades+=1;
			else
				break;
		return upgrades;
	}
}