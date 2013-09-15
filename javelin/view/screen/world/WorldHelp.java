package javelin.view.screen.world;

import javelin.controller.action.world.WorldAction;

public class WorldHelp extends WorldAction {

	public WorldHelp() {
		super("Help", new int[] {}, new String[] { "h" });
	}

	@Override
	public void perform(final WorldScreen screen) {
		screen.help(WorldScreen.ACTIONS);
	}
}
