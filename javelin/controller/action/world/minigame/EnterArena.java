package javelin.controller.action.world.minigame;

import java.awt.event.KeyEvent;

import javelin.Javelin;
import javelin.controller.exception.battle.StartBattle;
import javelin.controller.fight.minigame.ArenaFight;
import javelin.view.screen.WorldScreen;

/**
 * Allows access to the Arena at any point in time (unless in battle).
 *
 * @author alex
 */
public class EnterArena extends EnterMinigame {
	private static final String PROMPT = "Start an arena match?\n\nPress ENTER start or any other key to cancel...";

	/** Constructor. */
	public EnterArena() {
		super("Arena (mini-game)", new int[] { KeyEvent.VK_A },
				new String[] { "A" });
	}

	@Override
	public void perform(WorldScreen screen) {
		super.perform(screen);
		if (Javelin.DEBUG || Javelin.prompt(PROMPT) == '\n') {
			throw new StartBattle(new ArenaFight());
		}
	}
}
