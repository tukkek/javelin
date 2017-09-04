package javelin.view.mappanel;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;

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
	public boolean discovered;

	public Tile(final int xp, final int yp, final boolean discoveredp) {
		super();
		x = xp;
		y = yp;
		discovered = discoveredp;
		setFocusable(false);
	}

	@Override
	abstract public void paint(Graphics g);

	protected static void draw(final Graphics g, final Image i) {
		try {
			g.drawImage(i, 0, 0, MapPanel.tilesize, MapPanel.tilesize, null);
		} catch (NullPointerException e) {
			/**
			 * On Windows 8 this method can raise a "NullPointerException: HDC
			 * for component", which seems to be a system or JDK error. It's not
			 * clear how to fix it but another project seems to just ignore it
			 * so that's what I'm trying too.
			 * 
			 * More info https://netbeans.org/bugzilla/show_bug.cgi?id=165867
			 */
			e.printStackTrace();
			System.out.println("Graphics: " + g);
			System.out.println("Image: " + i);
			System.out.println("Tile size: " + MapPanel.tilesize);
		}
	}

	public void cover() {
		discovered = false;
		repaint();
	}
}
