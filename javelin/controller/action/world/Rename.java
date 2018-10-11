package javelin.controller.action.world;

import javelin.Javelin;
import javelin.controller.exception.RepeatTurn;
import javelin.model.unit.Combatant;
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
		int i=Javelin.choose("Rename which unit?",Squad.active.members,true,false);
		if(i==-1) throw new RepeatTurn();
		Combatant m=Squad.active.members.get(i);
		m.source.customName=NamingScreen.getname(m.source.toString());
	}
}
