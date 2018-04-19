package javelin.view.screen.wish;

import javelin.controller.challenge.RewardCalculator;
import javelin.model.unit.Squad;
import javelin.model.unit.attack.Combatant;

public class Gold extends Wish {

	public Gold(WishScreen screen) {
		super("Gold ($" + RewardCalculator.getgold(screen.rubies + 1) + ")",
				'g', screen.rubies, false, screen);
	}

	@Override
	protected boolean hack(Combatant target) {
		Squad.active.gold += RewardCalculator.getgold(screen.rubies + 1);
		return true;
	}
}
