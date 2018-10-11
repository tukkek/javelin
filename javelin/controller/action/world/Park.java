package javelin.controller.action.world;

import java.awt.event.KeyEvent;

import javelin.Javelin;
import javelin.controller.exception.RepeatTurn;
import javelin.model.unit.Squad;
import javelin.view.screen.WorldScreen;

/**
 * Park and exit your current vehicle.
 *
 * @author alex
 */
public class Park extends WorldAction{

	/** Constructor. */
	public Park(){
		super("Park your vehicle",new int[]{KeyEvent.VK_P},new String[]{"p"});
	}

	@Override
	public void perform(WorldScreen screen){
		if(Squad.active.transport==null) throw new RepeatTurn();
		try{
			Squad.active.transport.park();
		}catch(RepeatTurn e){
			Javelin.message(e.getMessage(),false);
			throw e;
		}
	}
}
