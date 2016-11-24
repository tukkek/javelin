package javelin.controller.action.world.minigame;

import com.sun.glass.events.KeyEvent;

import javelin.controller.action.world.WorldAction;
import javelin.model.world.location.unique.minigame.Arena;
import javelin.view.screen.WorldScreen;

/**
 * Allows access to the Arena at any point in time (unless in battle).
 * 
 * @author alex
 */
public class EnterArena extends WorldAction {
	/** Constructor. */
	public EnterArena() {
		super("Arena (mini-game)", new int[] { KeyEvent.VK_A },
				new String[] { "A" });
	}

	@Override
	public void perform(WorldScreen screen) {
		Arena.get().interact();
	}
}
