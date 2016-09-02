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
public class OpenArena extends WorldAction {
	/** Constructor. */
	public OpenArena() {
		super("Open Arena screen", new int[] { KeyEvent.VK_A },
				new String[] { "a" });
	}

	@Override
	public void perform(WorldScreen screen) {
		Arena.get().interact();
	}
}
