package javelin.controller.action.world;

import javelin.controller.action.ZoomOut;
import javelin.view.screen.WorldScreen;

public class ZoomWorldOut extends WorldAction{
	public ZoomWorldOut(){
		super("Zoom out",new int[]{},new String[]{"-"});
	}

	@Override
	public void perform(WorldScreen screen){
		ZoomOut.zoom(WorldScreen.current.getherolocation());
	}
}
