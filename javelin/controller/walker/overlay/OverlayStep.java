package javelin.controller.walker.overlay;

import java.awt.Color;

import javelin.controller.Point;

public class OverlayStep extends Point {
    public Color color;
    public String text;

    public OverlayStep(Point p) {
	super(p);
    }
}
