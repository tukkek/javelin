package javelin.view.mappanel.dungeon;

import java.util.HashSet;

import javelin.controller.db.Preferences;
import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

public class DungeonPanel extends MapPanel{
	Dungeon dungeon;
	HashSet<Tile> skip=new HashSet<>();

	public DungeonPanel(Dungeon d){
		super(d.size,d.size,Preferences.KEYTILEDUNGEON);
		dungeon=d;
	}

	@Override
	protected Mouse getmouselistener(){
		return new DungeonMouse(this);
	}

	@Override
	protected int gettilesize(){
		return Preferences.tilesizedungeons;
	}

	@Override
	protected Tile newtile(int x,int y){
		return new DungeonTile(x,y,this);
	}

	@Override
	public void setup(){
		super.setup();
		scroll.setVisible(false);
	}

	@Override
	public void refresh(){
		repaint();
	}

	@Override
	public void repaint(){
		// super.repaint();
		for(Tile[] ts:tiles)
			for(Tile t:ts){
				if(!t.discovered) continue;
				if(Dungeon.active.map[t.x][t.y]==MapTemplate.WALL
						&&Dungeon.active.features.get(t.x,t.y)==null&&!skip.add(t))
					continue;
				t.repaint();
			}
	}
}
