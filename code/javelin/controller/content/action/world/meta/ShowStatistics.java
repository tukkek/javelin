package javelin.controller.content.action.world.meta;

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
    var status=WorldScreen.showstatusinformation();
    var answer=Javelin.choose("Choose a character:",status,true,false);
    if(answer==-1) return;
    var combatant=Squad.active.members.get(answer);
    new StatisticsScreen().open(combatant);
  }
}
