package javelin.view.mappanel.dungeon;

import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonImages;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.feature.door.Door;
import javelin.view.Images;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Tile;

public class DungeonTile extends Tile{
	Image floorimage;
	Image wallimage;
	Image featureimage;

	public DungeonTile(int x,int y,DungeonPanel p){
		super(x,y,p.dungeon.visible[x][y]);
	}

	@Override
	public void paint(Graphics g){
		var floor=Dungeon.active;
		if(!discovered||floor==null){
			drawcover(g);
			return;
		}
		var d=floor.dungeon;
		var f=floor.features.get(x,y);
		if(floorimage==null){
			var folder=d instanceof Wilderness?"":"dungeon";
			floorimage=Images.get(List.of(folder,d.images.get(DungeonImages.FLOOR)));
			wallimage=Images.get(List.of(folder,d.images.get(DungeonImages.WALL)));
			if(f!=null) featureimage=f.getimage();
		}
		draw(g,floorimage);
		if(floor.map[x][y]==MapTemplate.WALL||f instanceof Door&&d.doorbackground)
			draw(g,wallimage);
		if(f!=null&&f.draw) draw(g,featureimage);
		if(floor.squadlocation.x==x&&floor.squadlocation.y==y)
			draw(g,Squad.active.getimage());
		if(MapPanel.overlay!=null) MapPanel.overlay.overlay(this);
	}
}
