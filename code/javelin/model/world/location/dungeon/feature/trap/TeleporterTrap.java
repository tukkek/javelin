package javelin.model.world.location.dungeon.feature.trap;

import java.util.ArrayList;

import javelin.Javelin;
import javelin.controller.DungeonMapCrawler;
import javelin.controller.Point;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.model.unit.Combatant;
import javelin.model.unit.Squad;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.old.RPG;

/**
 * Teleports the player to a random spot on the floor - making sure not to cross
 * any doors, preventing the player from becoming stuck.
 *
 * TODO magic traps in theory have some rules of their own
 *
 * @author alex
 */
public class TeleporterTrap extends Trap{
  class TeleportCrawl extends DungeonMapCrawler{
    public TeleportCrawl(){
      super(getlocation(),Integer.MAX_VALUE,Dungeon.active);
    }

    @Override
    protected boolean validate(Point p){
      if(dungeon.map[p.x][p.y]==FloorTile.WALL) return false;
      return super.validate(p);
    }

    @Override
    protected boolean validate(Feature f){
      return f==null||f==TeleporterTrap.this;
    }
  }

  public TeleporterTrap(int cr,DungeonFloor f){
    super(cr,"trap",f);
  }

  @Override
  protected void spring(){
    Combatant victim=null;
    for(var c:RPG.shuffle(new ArrayList<>(Squad.active.members))){
      var roll=RPG.r(1,20); //TODO extract save logic
      if(roll==20) continue;
      if(roll==1||roll+c.source.ref<savedc){
        victim=c;
        break;
      }
    }
    if(victim==null){
      Javelin.message("Your party evades the trap!",true);
      return;
    }
    Javelin.message(victim+" activates the teleportation trap!",true);
    var targets=new TeleportCrawl().crawl();
    targets.remove(new Point(x,y));
    var s=Dungeon.active.squadlocation;
    targets.remove(s);
    if(targets.isEmpty()) return;
    var targetlist=new ArrayList<>(targets);
    var a=RPG.pick(targetlist);
    var b=RPG.pick(targetlist);
    var to=a.distance(s)<b.distance(s)?a:b;
    Dungeon.active.teleport(to);
  }
}
