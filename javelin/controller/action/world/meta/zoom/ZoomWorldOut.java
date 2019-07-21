package javelin.controller.action.world.meta.zoom;

import javelin.controller.action.ZoomOut;
import javelin.controller.action.world.WorldAction;
import javelin.view.screen.WorldScreen;

public class ZoomWorldOut extends WorldAction{
	public ZoomWorldOut(){
		super("Zoom out",new int[]{},new String[]{"-"});
	}

	@Override
	public void perform(WorldScreen screen){
		ZoomOut.zoom(WorldScreen.current.getsquadlocation());
	}
}
