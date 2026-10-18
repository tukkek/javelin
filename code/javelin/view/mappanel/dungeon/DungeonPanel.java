package javelin.view.mappanel.dungeon;

import java.awt.Graphics;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.db.Preferences;
import javelin.model.world.location.dungeon.Dungeon;
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
    return new DungeonMouse();
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
    refresh(Dungeon.active.squadlocation,g);
  }

  /// Center every turn if there is no set {@link Grid#center}.
  static public void refresh(Point p,Graphics g){
    var tile=MapPanel.get().tiles[p.x][p.y];
    var grid=Grid.get();
    var center=grid.center;
    if(tile!=center&&center==null) grid.center(tile,true);
    var discovered=0;
    for(var tiles:MapPanel.get().tiles)
      for(var tile2:tiles) if(tile2.discovered) discovered+=1;
    grid.draw(tile,g);
  }
}
