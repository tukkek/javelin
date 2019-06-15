package javelin.controller.action.world;

import javelin.controller.action.Help;
import javelin.view.screen.WorldScreen;

/**
 * In-game help.
 *
 * @author alex
 */
public class WorldHelp extends WorldAction{
	/** Constructor. */
	public WorldHelp(){
		super("Help",new int[]{},new String[]{"h","?"});
	}

	@Override
	public void perform(final WorldScreen screen){
		Help.help(WorldAction.ACTIONS);
	}
}
