package javelin.controller.action.world.minigame;

import javelin.Javelin;
import javelin.controller.action.world.WorldAction;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.Run;
import javelin.model.world.location.unique.minigame.Ziggurat;
import javelin.view.screen.WorldScreen;

/**
 * @see Ziggurat
 * @author alex
 */
public class EnterZiggurat extends WorldAction {
	/** Constructor. */
	public EnterZiggurat() {
		super("Ziggurat (mini-game)", new int[] { 'Z' }, new String[] { "Z" });
	}

	@Override
	public void perform(WorldScreen screen) {
		if (Javelin.prompt("Start a ziggurat run?\n\n"//
				+ "Press ENTER to confirm or any other key to cancel...") == '\n') {
			throw new StartBattle(new Run());
		}
	}
}
