package javelin.controller.content.action.world.meta.zoom;

import javelin.controller.content.action.ZoomIn;
import javelin.controller.content.action.world.WorldAction;
import javelin.view.screen.WorldScreen;

public class ZoomWorldIn extends WorldAction{

	public ZoomWorldIn(){
		super("Zoom in",new int[]{},new String[]{"+"});
	}

	@Override
	public void perform(WorldScreen screen){
		ZoomIn.zoom(WorldScreen.current.getsquadlocation());
	}
}
