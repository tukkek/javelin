package javelin.view.mappanel;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import javelin.controller.Point;
import javelin.controller.walker.overlay.OverlayStep;
import javelin.controller.walker.overlay.OverlayWalker;
import javelin.view.screen.BattleScreen;

public class MoveOverlay extends Overlay{
	HashMap<Color,Border> BORDERS=new HashMap<>();

	public OverlayWalker path;
	public List<Point> steps;

	public MoveOverlay(OverlayWalker mover){
		path=mover;
	}

	public void walk(){
		steps=path.walk();
		try{
			for(Point step:steps){
				affected.add(step);
				BattleScreen.active.mappanel.tiles[step.x][step.y].repaint();
			}
		}catch(IndexOutOfBoundsException e){
			affected.clear();
		}
	}

	@Override
	public void overlay(Tile t){
		Graphics g=BattleScreen.active.mappanel.getdrawgraphics();
		Point p=t.getposition();
		for(Point step:steps){
			OverlayStep s=(OverlayStep)step;
			if(t.x!=s.x||t.y!=s.y) continue;
			Border border=BORDERS.get(s.color);
			if(border==null){
				border=BorderFactory.createLineBorder(s.color,3);
				BORDERS.put(s.color,border);
			}
			border.paintBorder(BattleScreen.active.mappanel.canvas,g,p.x,p.y,
					MapPanel.tilesize,MapPanel.tilesize);
			g.setColor(s.color);
			g.drawString(s.text,p.x+5,p.y+MapPanel.tilesize-5);
		}
	}

	public static void schedule(final MoveOverlay overlay){
		if(MapPanel.overlay!=null) MapPanel.overlay.clear();
		MapPanel.overlay=overlay;
		overlay.walk();
		for(Point p:MapPanel.overlay.affected)
			BattleScreen.active.mappanel.tiles[p.x][p.y].repaint();
	}

	public void reset(){
		path.reset();
		affected.clear();
	}
}
