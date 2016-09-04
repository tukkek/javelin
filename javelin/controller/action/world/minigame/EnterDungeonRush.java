package javelin.controller.action.world.minigame;

import javelin.controller.action.world.WorldAction;
import javelin.model.world.location.unique.minigame.DungeonRush;
import javelin.view.screen.WorldScreen;

/**
 * @see DungeonRush
 * @author alex
 */
public class EnterDungeonRush extends WorldAction {
	/** Constructor. */
	public EnterDungeonRush() {
		super("Dungeon rush (mini-game)", new int[] { 'R' },
				new String[] { "R" });
	}

	@Override
	public void perform(WorldScreen screen) {
		DungeonRush.start();
	}
}
