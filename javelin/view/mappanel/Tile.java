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
	final int x;
	final int y;

	public Tile(int xp, int yp) {
		super();
		x = xp;
		y = yp;
		setFocusable(false);
	}

	@Override
	abstract public void paint(Graphics g);
}
