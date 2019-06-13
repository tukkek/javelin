package javelin.view.mappanel;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.awt.ScrollPane;

import javelin.controller.db.Preferences;
import javelin.view.Images;
import javelin.view.mappanel.overlay.Overlay;

public abstract class MapPanel extends Panel{
	public static int tilesize=Preferences.tilesizeworld;

	public static Overlay overlay=null;

	public Tile[][] tiles=null;
	public ScrollPane scroll=new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
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
	public Canvas canvas=new Canvas(){
		@Override
		public void paint(Graphics g){
			super.paint(g);
			for(Tile[] ts:tiles)
				for(Tile t:ts)
					t.repaint();
		}
	};
	int mapwidth;
	int mapheight;

	protected boolean initial=true;

	/**
	 * Make sure we have a field for this to ensure we're going to instantiate
	 * {@link #tilesize}**2 listeners for this.
	 */
	Mouse mouse=getmouselistener();

	final private String configurationkey;

	public MapPanel(int widthp,int heightp,String configurationkeyp){
		mapwidth=widthp;
		mapheight=heightp;
		scroll.setFocusable(false);
		add(scroll);
		container.setFocusable(false);
		canvas.setFocusable(false);
		configurationkey=configurationkeyp;
	}

	abstract protected Mouse getmouselistener();

	void updatesize(){
		// container.setSize(tilesize * mapwidth, tilesize * mapheight);
		canvas.setSize(tilesize*mapwidth,tilesize*mapheight);
	}

	protected void updatetilesize(){
		try{
			/*
			 * TODO could use this to cache position instead of calculating on
			 * the fly
			 */
			updatesize();
			scroll.validate();
		}catch(NullPointerException e){
			return;
		}
	}

	public void setup(){
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

	protected abstract int gettilesize();

	protected abstract Tile newtile(int x,int y);

	public void viewposition(int x,int y){
		center(x,y,false);
	}

	public void setposition(int x,int y){
		center(x,y,true);
	}

	@Override
	public void update(Graphics g){
		repaint();
	}

	@Override
	public Dimension getPreferredSize(){
		try{
			return getParent().getBounds().getSize();
		}catch(NullPointerException e){
			return new Dimension(0,0);
		}
	}

	public void zoom(int factor,boolean save,int x,int y){
		tilesize+=factor*4;
		updatetilesize();
		center(x,y,true);
		Preferences.setoption(configurationkey,tilesize);
	}

	public boolean center(int x,int y,boolean force){
		int width=scroll.getWidth();
		int height=scroll.getHeight();
		java.awt.Point current=scroll.getScrollPosition();
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

	public void refresh(){
		if(initial){
			initial=false;
			scroll.setBounds(getBounds());
			int before=tilesize;
			if(tilesize!=before) updatetilesize();
		}
	}

	public Graphics getdrawgraphics(){
		return canvas.getGraphics();
	}
}
