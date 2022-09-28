package javelin.view.mappanel.dungeon;

import java.util.HashSet;

import javelin.controller.db.Preferences;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;

public class DungeonPanel extends MapPanel{
  DungeonFloor dungeon;
  HashSet<Tile> skip=new HashSet<>();

  public DungeonPanel(DungeonFloor d){
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
    synchronized(PAINTER){
      var p=scroll.getScrollPosition();
      var s=scroll.getViewportSize();
      var w=s.width;
      var h=s.height;
      for(Tile[] tiles:tiles) for(Tile tile:tiles){
        if(!tile.discovered) continue;
        var t=(DungeonTile)tile;
        if(p.x<=t.x*tilesize&&(t.x+1)*tilesize<=p.x+w&&p.y<=(t.y+1)*tilesize
            &&(t.y+1)*tilesize<=p.y+h)
          t.repaint();
      }
    }
  }
}
