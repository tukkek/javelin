package javelin.controller.wish;

import javelin.model.unit.Combatant;
import javelin.model.world.location.dungeon.feature.Fountain;

public class Heal extends Wish {
	public Heal(Character keyp, WishScreen screen) {
		super("Heal ally", keyp, 1, true, screen);
	}

	@Override
	boolean wish(Combatant target) {
		Fountain.heal(target);
		return true;
	}
}
