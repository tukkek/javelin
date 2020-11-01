package javelin.view.mappanel.dungeon;

import java.awt.Graphics;
import java.util.List;

import javelin.JavelinApp;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.Dungeon.DungeonImage;
import javelin.model.world.location.dungeon.Wilderness;
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
		var d=Dungeon.active;
		if(!discovered||d==null){
			drawcover(g);
			return;
		}
		var folder=d instanceof Wilderness?"":"dungeon";
		draw(g,Images.get(List.of(folder,d.images.get(DungeonImage.FLOOR))));
		draw(g,JavelinApp.context.gettile(x,y));
		final Feature f=d.features.get(x,y);
		if(f!=null&&f.draw){
			if(f instanceof Door&&d.doorbackground)
				draw(g,Images.get(List.of(folder,d.images.get(DungeonImage.WALL))));
			draw(g,f.getimage());
		}
		if(d.squadlocation.x==x&&d.squadlocation.y==y){
			Squad.active.updateavatar();
			draw(g,Squad.active.getimage());
		}
		if(MapPanel.overlay!=null) MapPanel.overlay.overlay(this);
	}
}
