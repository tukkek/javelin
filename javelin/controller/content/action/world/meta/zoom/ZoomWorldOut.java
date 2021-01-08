package javelin.controller.content.action.world.meta.zoom;

import javelin.controller.content.action.ZoomOut;
import javelin.controller.content.action.world.WorldAction;
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
