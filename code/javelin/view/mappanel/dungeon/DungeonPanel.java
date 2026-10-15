package javelin.view.mappanel.dungeon;

import java.awt.Graphics;
import java.util.HashSet;

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
  public int gettilesize(){
    return Preferences.tilesizedungeons;
  }

  @Override
  protected void settilesize(int size){
    Preferences.tilesizedungeons=size;
  }

  @Override
  protected Tile newtile(int x,int y){
    return new DungeonTile(x,y);
  }

  @Override
  public void refresh(Graphics g){
    var p=scroll.getScrollPosition();
    var s=scroll.getViewportSize();
    var w=s.width;
    var h=s.height;
    var size=MapPanel.get().gettilesize();
    for(Tile[] tiles:tiles) for(Tile tile:tiles){
      if(!tile.discovered) continue;
      var t=(DungeonTile)tile;
      if(p.x<=t.x*size&&(t.x+1)*size<=p.x+w&&p.y<=(t.y+1)*size&&t.y*size<=p.y+h)
        t.repaint(g);
    }
  }
}
