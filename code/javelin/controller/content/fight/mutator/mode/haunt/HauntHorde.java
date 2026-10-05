package javelin.controller.content.fight.mutator.mode.haunt;

import javelin.controller.content.fight.mutator.mode.Horde;
import javelin.controller.generator.encounter.EncounterGenerator.MonsterPool;
import javelin.model.world.location.haunt.Haunt;

public class HauntHorde extends Horde{
	/**
	 *
	 */
	private final Haunt haunt;

	public HauntHorde(Haunt haunt){
		super(haunt.targetel,new MonsterPool(haunt.pool));
		this.haunt=haunt;
	}
}