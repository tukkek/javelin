package javelin.view.mappanel.dungeon;

import java.awt.Graphics;
import java.util.HashSet;

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
    var point=Dungeon.active.squadlocation;
    var tile=MapPanel.get().tiles[point.x][point.y];
    var grid=Grid.get();
    grid.center(tile,grid.center!=tile);
    grid.draw(tile,g);
  }
}
