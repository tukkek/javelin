package javelin.controller.content.template;

import javelin.controller.content.kit.Kit;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.old.RPG;

/**
 * Applies one of their preferred {@link Kit}s to each {@link Combatant}.
 *
 * @see Kit#getpreferred(javelin.model.unit.Monster, boolean)
 * @author alex
 */
public class KitTemplate extends Template{
	/** Unique instance. */
	public static final KitTemplate SINGLETON=new KitTemplate();

	/** Constructor. */
	KitTemplate(){
		super(null);
	}

	@Override
	public int apply(Combatant c,Dungeon d){
		var k=RPG.pick(Kit.getpreferred(c.source,true));
		var cr=c.source.cr;
		var target=cr*2;
		if(d!=null) target=Math.min(target,cr+d.level/2);
		var upgrades=0;
		while(c.source.cr<target&&k.upgrade(c))
			upgrades+=1;
		return upgrades;
	}
}