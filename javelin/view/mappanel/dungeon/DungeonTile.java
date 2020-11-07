package javelin.view.mappanel.dungeon;

import java.awt.Graphics;
import java.util.List;

import javelin.JavelinApp;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonImages;
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
		var floor=Dungeon.active;
		if(!discovered||floor==null){
			drawcover(g);
			return;
		}
		var d=floor.dungeon;
		var folder=d instanceof Wilderness?"":"dungeon";
		var images=d.images;
		draw(g,Images.get(List.of(folder,images.get(DungeonImages.FLOOR))));
		draw(g,JavelinApp.context.gettile(x,y));
		final Feature f=floor.features.get(x,y);
		if(f!=null&&f.draw){
			if(f instanceof Door&&d.doorbackground)
				draw(g,Images.get(List.of(folder,images.get(DungeonImages.WALL))));
			draw(g,f.getimage());
		}
		if(floor.squadlocation.x==x&&floor.squadlocation.y==y){
			Squad.active.updateavatar();
			draw(g,Squad.active.getimage());
		}
		if(MapPanel.overlay!=null) MapPanel.overlay.overlay(this);
	}
}
