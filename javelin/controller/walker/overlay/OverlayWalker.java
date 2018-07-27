package javelin.controller.walker.overlay;

import javelin.controller.Point;
import javelin.controller.walker.Walker;

public abstract class OverlayWalker extends Walker {
    public OverlayWalker(Point from, Point to) {
	super(from, to);
	optimize = false;
	includetarget = true;
	acceptpartial = true;
    }

    abstract public Point resetlocation();
}
