package javelin.controller.action.world;

import javelin.controller.action.ZoomIn;
import javelin.view.screen.WorldScreen;

public class ZoomWorldIn extends WorldAction {

	public ZoomWorldIn() {
		super("Zoom in", new int[] {}, new String[] { "+" });
	}

	@Override
	public void perform(WorldScreen screen) {
		ZoomIn.zoom(WorldScreen.current.getherolocation());
	}
}
