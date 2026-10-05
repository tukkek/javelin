package javelin.controller.content.action.world.meta;

import javelin.Javelin;
import javelin.controller.content.action.world.WorldAction;
import javelin.model.unit.Squad;
import javelin.view.screen.NamingScreen;
import javelin.view.screen.WorldScreen;

/**
 * Rename combatant.
 *
 * @author alex
 */
public class Rename extends WorldAction{
  /** Constructor. */
  public Rename(){
    super("Rename squad members",new int[]{},new String[]{"r"});
  }

  @Override
  public void perform(WorldScreen screen){
    var i=Javelin.choose("Rename which unit?",Squad.active.members,true,false);
    if(i==-1) return;
    var m=Squad.active.members.get(i);
    m.source.customName=NamingScreen.getname(m.source.toString());
    Squad.active.sort();
  }
}
