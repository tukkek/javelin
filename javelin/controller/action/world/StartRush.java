package javelin.controller.action.world;

import javelin.model.world.location.unique.minigame.DungeonRush;
import javelin.view.screen.WorldScreen;

/**
 * @see DungeonRush
 * @author alex
 */
public class StartRush extends WorldAction {
	/** Constructor. */
	public StartRush() {
		super("Start a dungeon rush match", new int[] { 'R' },
				new String[] { "R" });
	}

	@Override
	public void perform(WorldScreen screen) {
		DungeonRush.start();
	}
}
