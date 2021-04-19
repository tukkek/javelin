package javelin.controller.content.fight.mutator.mode.haunt;

import javelin.controller.content.fight.mutator.mode.Gauntlet;
import javelin.controller.generator.encounter.EncounterGenerator.MonsterPool;
import javelin.model.item.Item;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.haunt.Haunt;

/**
 * TODO once haunts are {@link Branch}-based, use {@link Branch#treasure} for
 * item rewards and {@link Branch#prefix} for voice adjective.
 */
public class HauntGauntlet extends Gauntlet{
	final Haunt haunt;

	public HauntGauntlet(Haunt haunt){
		super(haunt.targetel,new MonsterPool(haunt.pool),Item.ITEMS,"haunting");
		this.haunt=haunt;
	}
}