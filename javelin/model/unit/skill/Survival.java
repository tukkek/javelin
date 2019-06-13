package javelin.model.unit.skill;

import javelin.controller.terrain.Terrain;

/**
 * A skill measuring the ability to have the {@link Terrain} around you help -
 * or, similarly, to prevent it from harming you.
 *
 * @see Terrain#survivalbonus
 * @author alex
 */
public class Survival extends Skill{
	/** Constructor. */
	public Survival(){
		super("Survival",Ability.WISDOM);
	}
}
