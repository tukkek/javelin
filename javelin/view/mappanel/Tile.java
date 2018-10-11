package javelin.view.mappanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javelin.controller.Point;
import javelin.model.state.Square;
import javelin.view.screen.BattleScreen;

/**
 * Similar to a {@link Square} but for the view layer instead of the controller
 * layer.
 *
 * @author alex
 */
public abstract class Tile{
	public final int x;
	public final int y;
	public boolean discovered;

	public Tile(final int xp,final int yp,final boolean discoveredp){
		super();
		x=xp;
		y=yp;
		discovered=discoveredp;
	}

	abstract public void paint(Graphics c);

	protected void draw(final Graphics g,final Image i){
		try{
			Point p=getposition();
			g.drawImage(i,p.x,p.y,MapPanel.tilesize,MapPanel.tilesize,null);
		}catch(NullPointerException e){
			/**
			 * On Windows 8 this method can raise a "NullPointerException: HDC for
			 * component", which seems to be a system or JDK error. It's not clear how
			 * to fix it but another project seems to just ignore it so that's what
			 * I'm trying too.
			 *
			 * More info https://netbeans.org/bugzilla/show_bug.cgi?id=165867
			 */
			e.printStackTrace();
			System.out.println("Image: "+i);
			System.out.println("Tile size: "+MapPanel.tilesize);
		}
	}

	public void cover(){
		discovered=false;
	}

	public void repaint(){
		paint(BattleScreen.active.mappanel.getdrawgraphics());
	}

	/**
	 * TODO might decide to turn into field instead of generating so many times.
	 */
	public Point getposition(){
		return new Point(x*MapPanel.tilesize,y*MapPanel.tilesize);
	}

	protected void drawcover(final Graphics g){
		Point p=getposition();
		g.setColor(Color.BLACK);
		g.fillRect(p.x,p.y,MapPanel.tilesize,MapPanel.tilesize);
	}
}
