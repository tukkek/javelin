package javelin.view.mappanel;

import java.awt.Image;
import java.util.ArrayList;

import javelin.controller.Point;
import javelin.view.screen.BattleScreen;

public abstract class Overlay{
	public ArrayList<Point> affected=new ArrayList<>();

	abstract public void overlay(Tile t);

	public void clear(){
		MapPanel.overlay=null;
		final Tile[][] tiles=BattleScreen.active.mappanel.tiles;
		for(Point p:new ArrayList<>(affected))
			try{
				tiles[p.x][p.y].repaint();
			}catch(IndexOutOfBoundsException e){
				continue;// TODO
			}
		BattleScreen.active.mappanel.refresh();
	}

	/**
	 * Draws image on given tile and adds it to #affected.
	 */
	protected void draw(Tile t,Image i){
		Point p=t.getposition();
		BattleScreen.active.mappanel.getdrawgraphics().drawImage(i,p.x,p.y,
				MapPanel.tilesize,MapPanel.tilesize,null);
		affected.add(new Point(t.x,t.y));
	}

	public void refresh(MapPanel mappanel){
		for(Point p:affected)
			mappanel.tiles[p.x][p.y].repaint();
	}
}
