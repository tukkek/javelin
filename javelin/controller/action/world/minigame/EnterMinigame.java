package javelin.controller.action.world.minigame;

import javelin.Javelin;
import javelin.controller.action.world.WorldAction;
import javelin.controller.exception.RepeatTurn;
import javelin.model.world.World;
import javelin.view.screen.WorldScreen;

public class EnterMinigame extends WorldAction {
	public EnterMinigame(String name, int[] keysp, String[] morekeysp) {
		super(name, keysp, morekeysp);
	}

	@Override
	public void perform(WorldScreen screen) {
		if (World.scenario.minigames) {
			Javelin.message("Mini-games are not available on scenario mode.",
					false);
			throw new RepeatTurn();
		}
	}
}
