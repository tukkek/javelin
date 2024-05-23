package javelin.view.mappanel.dungeon;

import java.awt.Color;
import java.util.LinkedList;

import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.walker.overlay.OverlayStep;
import javelin.controller.walker.overlay.OverlayWalker;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.Wilderness.Path;
import javelin.model.world.location.dungeon.feature.trap.MechanicalTrap;

public class DungeonWalker extends OverlayWalker{
  class DungeonStep extends OverlayStep{
    public float encounterchance;

    public DungeonStep(Point p){
      super(p);
    }
  }

  /**
   * If <code>true</code> (default), only {@link DungeonFloor#discovered} tiles
   * are allowed.
   */
  public boolean discoveredonly=true;

  DungeonFloor floor;

  public DungeonWalker(Point from,Point to,DungeonFloor f){
    super(from,to);
    floor=f;
  }

  @Override
  protected Point step(Point p,LinkedList<Point> previous){
    var step=new DungeonStep(p);
    var chance=previous.isEmpty()?0
        :((DungeonStep)previous.getLast()).encounterchance;
    chance+=1f/floor.stepsperencounter;//
    step.encounterchance=chance;
    step.text=Math.round((chance>1?1:chance)*100)+"%";
    step.color=chance>=.5?Color.RED:Color.GREEN;
    return step;
  }

  @Override
  public boolean validate(Point step,LinkedList<Point> previous){
    var p=(DungeonStep)step;
    if(floor.map[p.x][p.y]==FloorTile.WALL||p.encounterchance>1) return false;
    if(p.equals(to)) return !floor.squadlocation.equals(step);
    if(discoveredonly&&!Dungeon.active.visible[p.x][p.y]) return false;
    final var f=floor.features.get(p.x,p.y);
    return f==null||f instanceof MechanicalTrap||f instanceof Path;
  }

  @Override
  public Point resetlocation(){ // TODO
    return floor==null?null:floor.squadlocation;
  }
}
