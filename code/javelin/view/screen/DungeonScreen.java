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
    floor=f;
    open();
  }

  @Override
  public boolean explore(int x,int y){
    return floor.explore();
  }

  @Override
  public boolean react(int x,int y){
    var p=Skill.PERCEPTION;
    var unit=Squad.active.getbest(p);
    var r=unit.roll(p);
    var features=floor.features;
    var near=features.stream().filter(f->Walker.distanceinsteps(x,y,f.x,f.y)==1)
        .toList();
    for(var feature:near) feature.discover(unit,r);
    var f=features.get(x,y);
    if(f==null) return false;
    if(f.activate()&&f.remove) features.remove(f);
    if(WorldMove.abort) return true;
    if(f.enter) WorldMove.place(x,y);
    else WorldMove.abort=true;
    return true;
  }

  @Override
  public boolean allowmove(int x,int y){
    return floor.map[x][y]!=FloorTile.WALL;
  }

  @Override
  public void updatelocation(int x,int y){
    var point=floor.squadlocation;
    point.x=x;
    point.y=y;
  }

  @Override
  public void view(int xp,int yp){
    var point=floor.squadlocation;
    var v=floor.dungeon.vision;
    for(var p:Brazier.reveal(point,v)) floor.setvisible(p.x,p.y);
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
