package javelin.view.mappanel;

import java.awt.Canvas;
import java.awt.Graphics;

import javelin.model.state.Square;

/**
 * Similar to a {@link Square} but for the view layer instead of the controller
 * layer.
 * 
 * @author alex
 */
public abstract class Tile extends Canvas {
	public final int x;
	public final int y;
	boolean discovered;

	public Tile(final int xp, final int yp, final boolean discoveredp) {
		super();
		x = xp;
		y = yp;
		discovered = discoveredp;
		setFocusable(false);
	}

	@Override
	abstract public void paint(Graphics g);
}