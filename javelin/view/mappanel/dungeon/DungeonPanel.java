package javelin.view.mappanel.dungeon;

import java.util.HashSet;

import javelin.Javelin;
import javelin.controller.db.Preferences;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.Mouse;
import javelin.view.mappanel.Tile;
import javelin.view.screen.BattleScreen;

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
      for(Tile[] tiles:tiles) for(Tile tile:tiles){
        var t=(DungeonTile)tile;
        if(t.discovered&&p.x<=t.x*tilesize&&(t.x+1)*tilesize<=p.x+s.width
            &&p.y<=t.y*tilesize&&(t.y+1)*tilesize<=p.y+s.height)
          t.repaint();
      }
    }
  }
}
