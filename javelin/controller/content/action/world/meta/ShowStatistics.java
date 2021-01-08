package javelin.controller.content.action.world.meta;

import java.util.List;

import javelin.Javelin;
import javelin.controller.content.action.world.WorldAction;
import javelin.model.unit.Squad;
import javelin.view.screen.StatisticsScreen;
import javelin.view.screen.WorldScreen;

/**
 * Show unit information.
 *
 * @author alex
 */
public class ShowStatistics extends WorldAction{
	/** Constructor. */
	public ShowStatistics(){
		super("View characters",new int[0],new String[]{"v"});
	}

	@Override
	public void perform(WorldScreen screen){
		List<String> status=WorldScreen.showstatusinformation();
		int answer=Javelin.choose("Choose a character:",status,true,false);
		if(answer!=-1) new StatisticsScreen(Squad.active.members.get(answer));
	}
}
