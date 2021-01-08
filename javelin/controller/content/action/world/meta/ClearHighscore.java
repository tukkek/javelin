package javelin.controller.content.action.world.meta;

import javelin.Javelin;
import javelin.controller.Highscore;
import javelin.controller.content.action.world.WorldAction;
import javelin.old.messagepanel.MessagePanel;
import javelin.view.screen.InfoScreen;
import javelin.view.screen.WorldScreen;

/**
 * Clear highscore.
 *
 * @author alex
 */
public class ClearHighscore extends WorldAction{

	/** Constructor. */
	public ClearHighscore(){
		super(name(),new int[0],new String[]{"C"});
	}

	static String name(){
		return "Clear highscore ("+Highscore.gethighscore()+")";
	}

	@Override
	public void perform(WorldScreen screen){
		MessagePanel.active.clear();
		Javelin.message(
				"Are you sure you want to reset your highscore ("
						+Highscore.gethighscore()+")? Press y to proceed.\n",
				Javelin.Delay.NONE);
		boolean ok=InfoScreen.feedback()=='y';
		if(ok) Highscore.sethighscore(0);
		Javelin.message((ok?"Score reset":"Aborted")+"...",Javelin.Delay.WAIT);
		name=name();
	}

}
