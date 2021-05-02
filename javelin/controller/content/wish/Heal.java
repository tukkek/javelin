package javelin.controller.content.wish;

import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.feature.rare.Fountain;

/**
 * TODO allow details to be added to member selection
 *
 * @author alex
 */
public class Heal extends Wish{
	public Heal(Character keyp,WishScreen screen){
		super("heal ally",keyp,1,true,screen);
	}

	@Override
	boolean wish(Combatant target){
		Fountain.heal(target);
		return true;
	}
}
