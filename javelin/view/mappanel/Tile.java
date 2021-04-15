package javelin.view.mappanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.HashMap;

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
	public static HashMap<Image,Image> cache=new HashMap<>();

	public final int x;
	public final int y;
	public boolean discovered;

	public Tile(final int xp,final int yp,final boolean discoveredp){
		x=xp;
		y=yp;
		discovered=discoveredp;
	}

	abstract public void paint(Graphics c);

	synchronized protected void draw(final Graphics g,final Image i){
		var size=MapPanel.tilesize;
		try{
			var scaled=cache.get(i);
			if(scaled==null){
				scaled=i.getScaledInstance(size,size,Image.SCALE_DEFAULT);
				cache.put(i,scaled);
			}
			var p=getposition();
			g.drawImage(scaled,p.x,p.y,size,size,null);
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
			System.out.println("Tile size: "+size);
		}
	}

	public void cover(){
		discovered=false;
	}

	public void repaint(){
		try{
			paint(BattleScreen.active.mappanel.getdrawgraphics());
		}catch(NullPointerException e){
			var debug="Screen: "+BattleScreen.active;
			if(BattleScreen.active!=null){
				debug+="\nPanel: "+BattleScreen.active.mappanel;
				if(BattleScreen.active.mappanel!=null)
					debug+="\nGraphics: "+BattleScreen.active.mappanel.getdrawgraphics();
			}
			throw new RuntimeException(debug,e);
		}
	}

	/**
	 * TODO might decide to turn into field instead of generating so many times.
	 */
	public Point getposition(){
		return new Point(x*MapPanel.tilesize,y*MapPanel.tilesize);
	}

	protected void drawcover(final Graphics g){
		if(g==null) return;
		var p=getposition();
		g.setColor(Color.BLACK);
		g.fillRect(p.x,p.y,MapPanel.tilesize,MapPanel.tilesize);
	}
}
