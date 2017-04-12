package javelin.controller.action.world.minigame;

import javelin.model.world.location.unique.minigame.DungeonRush;
import javelin.view.screen.WorldScreen;

/**
 * @see DungeonRush
 * @author alex
 */
public class EnterDungeonRush extends EnterMinigame {
	/** Constructor. */
	public EnterDungeonRush() {
		super("Dungeon rush (mini-game)", new int[] { 'R' },
				new String[] { "R" });
	}

	@Override
	public void perform(WorldScreen screen) {
		super.perform(screen);
		DungeonRush.start();
	}
}
