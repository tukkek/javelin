package javelin.view.screen;

import java.awt.Image;
import java.util.HashSet;

import javelin.controller.Point;
import javelin.controller.content.action.world.WorldMove;
import javelin.controller.content.fight.Fight;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.walker.Walker;
import javelin.model.unit.Squad;
import javelin.model.unit.skill.Skill;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.common.Brazier;
import javelin.view.mappanel.MapPanel;
import javelin.view.mappanel.dungeon.DungeonPanel;

/**
 * Shows the inside of a {@link DungeonFloor}.
 *
 * @author alex
 */
public class DungeonScreen extends WorldScreen{
  public DungeonFloor floor;

  /** Constructor. */
  public DungeonScreen(DungeonFloor f){
    super(false);
    floor=f;
    open();
  }

  @Override
  public boolean explore(int x,int y){
    return floor.explore();
  }

  @Override
  public boolean react(int x,int y){
    var searching=Squad.active.getbest(Skill.PERCEPTION);
    for(var f:floor.features.copy()) if(Walker.distanceinsteps(x,y,f.x,f.y)==1)
      f.discover(searching,searching.roll(Skill.PERCEPTION));
    var f=floor.features.get(x,y);
    if(f==null) return false;
    var activated=f.activate();
    if(activated&&f.remove) floor.features.remove(f);
    if(!f.enter) WorldMove.abort=true;
    if(!activated) return false;
    if(!WorldMove.abort) WorldMove.place(x,y);
    return true;
  }

  @Override
  public boolean allowmove(int x,int y){
    return floor.map[x][y]!=FloorTile.WALL;
  }

  @Override
  public void updatelocation(int x,int y){
    floor.squadlocation.x=x;
    floor.squadlocation.y=y;
  }

  @Override
  public void view(int xp,int yp){
    for(var p:Brazier.reveal(floor.squadlocation,floor.dungeon.vision))
      floor.setvisible(p.x,p.y);
  }

  @Override
  public Image gettile(int x,int y){
    //handled by DungeonTile
    throw new UnsupportedOperationException();
  }

  @Override
  public Fight encounter(){
    return floor.dungeon.fight();
  }

  @Override
  protected MapPanel getmappanel(){
    return new DungeonPanel(floor);
  }

  @Override
  public boolean validatepoint(int x,int y){
    return 0<=x&&x<floor.size&&0<=y&&y<floor.size;
  }

  @Override
  public Point getsquadlocation(){
    return floor.squadlocation;
  }

  @Override
  protected HashSet<Point> getdiscovered(){
    return floor.discovered;
  }
}
