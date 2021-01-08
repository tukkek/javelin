package javelin.controller.content.action.world.meta;

import javelin.Javelin;
import javelin.controller.content.action.world.WorldAction;
import javelin.controller.db.StateManager;
import javelin.view.screen.WorldScreen;

/**
 * Quit the current game forever, delete the save game so the player can start a
 * new campaign.
 *
 * @author alex
 */
public class Abandon extends WorldAction{
	/** Constructor. */
	public Abandon(){
		super("Abandon current game",new int[]{},new String[]{"Q"});
	}

	@Override
	public void perform(final WorldScreen screen){
		String prompt="Are you sure you want to permanently abandon the current game?\n"
				+"Press c to confirm or any other key to cancel...";
		if(Javelin.prompt(prompt)=='c'){
			StateManager.abandoned=true;
			StateManager.save(true);
			System.exit(0);
		}
	}
}
