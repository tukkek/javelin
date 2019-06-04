package javelin.view.mappanel.dungeon;

import java.awt.Graphics;

import javelin.JavelinApp;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;

public class DungeonTile extends Tile{
	public DungeonTile(int xp,int yp,DungeonPanel p){
		super(xp,yp,p.dungeon.visible[xp][yp]);
	}

	@Override
	public void paint(Graphics g){
		if(!discovered||Dungeon.active==null){
			drawcover(g);
			return;
		}
		draw(g,JavelinApp.context.gettile(x,y));
		if(Dungeon.active==null) return;
		final Feature f=Dungeon.active.features.get(x, y);
		if(f!=null&&f.draw){
			if(f instanceof Door&&Dungeon.active.doorbackground)
				draw(g,Images.get(Dungeon.active.walltile));
			draw(g,Images.get(f.avatarfile));
		}
		if(Dungeon.active.herolocation.x==x&&Dungeon.active.herolocation.y==y){
			Squad.active.updateavatar();
			draw(g,Squad.active.getimage());
		}
		if(MapPanel.overlay!=null) MapPanel.overlay.overlay(this);
	}
}
