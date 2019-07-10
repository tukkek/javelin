package javelin.controller.action;

import javelin.controller.Point;
import javelin.model.unit.Combatant;
import javelin.view.screen.BattleScreen;

/**
 * @see ZoomOut
 * @author alex
 */
public class ZoomIn extends Action{

	/** Constructor. */
	public ZoomIn(){
		super("Zoom in",new String[]{"+","="});
		allowburrowed=true;
	}

	@Override
	public boolean perform(Combatant active){
		zoom(active.getlocation());
		return true;
	}

	public static void zoom(Point p){
		BattleScreen.active.mappanel.zoom(+1,p.x,p.y,true);
	}
}
