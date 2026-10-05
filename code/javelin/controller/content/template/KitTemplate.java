package javelin.controller.content.template;

import java.math.BigDecimal;

import javelin.controller.content.kit.Kit;
import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.old.RPG;

/**
 * Applies one of their preferred {@link Kit}s to each {@link Combatant}.
 *
 * TODO remove once all Temples have {@link Branch#templates}?
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
  public int apply(Combatant c){
    c.clonesource();
    var k=RPG.pick(Kit.getpreferred(c.source,true));
    var cr=Math.round(c.source.cr);
    c.xp=new BigDecimal(RPG.r(cr/2,cr));
    var upgrades=0;
    while(c.xp.floatValue()>0&&k.upgrade(c,true)) upgrades+=1;
    return upgrades;
  }
}
