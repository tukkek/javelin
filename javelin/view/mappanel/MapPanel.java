package javelin.view.mappanel;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.view.Images;
import javelin.view.mappanel.overlay.Overlay;

/**
 * Game map view, either in or out of battle.
 *
 * @author alex
 */
public abstract class MapPanel extends Panel{
	/** Current tile size. */
	public static int tilesize=Preferences.tilesizeworld;
	/** Produces a temporary overlay effect on-screen. */
	public static Overlay overlay=null;

	/** Grid of tiles by coordinate. */
	public Tile[][] tiles=null;
	/** Scrollbar functionality. */
	public ScrollPane scroll=new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
	/** Main component for panel. */
	public Canvas canvas=new Canvas(){
		@Override
		public void paint(Graphics g){
			super.paint(g);
			for(Tile[] ts:tiles)
				for(Tile t:ts)
					t.repaint();
		}
	};

	Panel container=new Panel(){
		@Override
		public void paint(Graphics g){
			Image texture=Images.TEXTUREMAP;
			for(int x=0;x<getWidth();x+=texture.getWidth(null))
				for(int y=0;y<getHeight();y+=texture.getHeight(null))
					g.drawImage(texture,x,y,null);
			super.paint(g);
		}
	};
	/**
	 * Make sure we have a field for this to ensure we're going to instantiate
	 * {@link #tilesize}**2 listeners for this.
	 */
	Mouse mouse=getmouselistener();
	String configurationkey;
	int mapwidth;
	int mapheight;

	/** Constructor. */
	public MapPanel(int widthp,int heightp,String configurationkeyp){
		mapwidth=widthp;
		mapheight=heightp;
		scroll.setFocusable(false);
		add(scroll);
		container.setFocusable(false);
		canvas.setFocusable(false);
		configurationkey=configurationkeyp;
	}

	/** @return Reactions to mouse events. */
	abstract protected Mouse getmouselistener();

	void updatesize(){
		canvas.setSize(tilesize*mapwidth,tilesize*mapheight);
	}

	/**
	 * TODO could use this to cache position instead of calculating on the fly
	 */
	protected void updatetilesize(){
		try{
			updatesize();
			scroll.validate();
		}catch(NullPointerException e){
			return;
		}
	}

	/** Configures size, scrolling, listeners, etc. */
	public void setup(){
		addComponentListener(new ComponentAdapter(){
			@Override
			public void componentShown(ComponentEvent e){
				scroll.setBounds(getBounds());
			}

			@Override
			public void componentResized(ComponentEvent e){
				componentShown(e);
			}
		});
		tilesize=gettilesize();
		scroll.setVisible(false);
		updatesize();
		tiles=new Tile[mapwidth][mapheight];
		for(int y=0;y<mapheight;y++)
			for(int x=0;x<mapwidth;x++)
				tiles[x][y]=newtile(x,y);
		container.add(canvas);
		canvas.addMouseListener(mouse);
		canvas.addMouseMotionListener(mouse);
		canvas.addMouseWheelListener(mouse);
		scroll.add(container);
		scroll.setVisible(true);
	}

	/** @see Preferences */
	protected abstract int gettilesize();

	/** TODO can probably use reflection instead */
	protected abstract Tile newtile(int x,int y);

	/** Non-forceful {@link #center(int, int, boolean)}. */
	public void viewposition(int x,int y){
		center(x,y,false);
	}

	/** Forceful {@link #center(int, int, boolean)}. */
	public void setposition(int x,int y){
		center(x,y,true);
	}

	@Override
	public Dimension getPreferredSize(){
		try{
			return getParent().getBounds().getSize();
		}catch(NullPointerException e){
			return new Dimension(0,0);
		}
	}

	/**
	 * @param factor How much to zoom in (positive) or out (negative) for. 0
	 *          results in no changes.
	 * @param x Coordinate to center on.
	 * @param y Coordinate to center on.
	 * @param redraw <code>true</code> to {@link Javelin#redraw()}.
	 */
	public void zoom(int factor,int x,int y){
		Tile.cache.clear();
		tilesize+=factor*4;
		updatetilesize();
		center(x,y,true);
		Preferences.setoption(configurationkey,tilesize);
	}

	/**
	 * @param force If false, will only center if coordinates are out-of-view.
	 * @return Whether a scroll reposition has been performed.
	 */
	public boolean center(int x,int y,boolean force){
		int width=scroll.getWidth();
		int height=scroll.getHeight();
		var current=scroll.getScrollPosition();
		x*=tilesize;
		y*=tilesize;
		if(!force&&isinside(current.getX(),x,width-tilesize*2)
				&&isinside(current.getY(),y,height-tilesize*2)){
			scroll.setScrollPosition(current);
			return false;
		}
		x-=width/2;
		y-=height/2;
		x=Math.min(x,container.getWidth()-width+scroll.getHScrollbarHeight());
		y=Math.min(y,container.getHeight()-height+scroll.getVScrollbarWidth());
		x=Math.max(0,x);
		y=Math.max(0,y);
		scroll.setScrollPosition(x,y);
		return true;
	}

	static private boolean isinside(double from,int value,int offset){
		return from<=value&&value<=from+offset;
	}

	/** Updates {@link Tile}s without redrawing the whole screen. */
	abstract public void refresh();

	/** @return {@link Canvas#getGraphics()}. */
	public Graphics getdrawgraphics(){
		return canvas.getGraphics();
	}
}
