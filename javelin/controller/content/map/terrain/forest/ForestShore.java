package javelin.controller.content.map.terrain.forest;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.Point;
import javelin.controller.content.fight.Fight;
import javelin.controller.content.map.DndMap;
import javelin.controller.content.map.Map;
import javelin.controller.content.terrain.Forest;
import javelin.controller.content.terrain.Water;
import javelin.model.state.Square;
import javelin.old.RPG;
import javelin.view.Images;
import javelin.view.mappanel.world.WorldTile;

/**
 * {@link Fight} map for {@link Forest} {@link WorldTile}s next to
 * {@link Water}.
 *
 * @author alex
 */
public class ForestShore extends Map{
  static final int SIZE=DndMap.SIZE;

  /** Constructor. */
  public ForestShore(){
    super("Forest shore",SIZE,SIZE);
    wall=Images.get(List.of("terrain","tree"));
    floor=Images.get(List.of("terrain","desert"));
    flooded=Images.get(List.of("terrain","aquatic"));
  }

  List<Square> clear(int beachy,int stepx,int stepy,int variance,int fromy,
      int toy){
    var wave=new ArrayList<Square>();
    for(var p=new Point(SIZE/2,beachy);0<=p.x&&p.x<SIZE;p.x+=stepx){
      p.y+=RPG.r(-variance,+variance);
      p.bind(SIZE,toy);
      for(var y=p.y;fromy<=y&&y<toy;y+=stepy) wave.add(map[p.x][y]);
    }
    return wave;
  }

  void drawtreeline(int beachy){
    for(var p:Point.getrange(SIZE,beachy)){
      var m=map[p.x][p.y];
      if(RPG.chancein(8)) m.blocked=true;
      else m.obstructed=true;
    }
    var crest=RPG.high(0,beachy);
    var wave=clear(crest,+1,+1,1,0,beachy);
    wave.addAll(clear(crest,-1,+1,1,0,beachy));
    for(var w:wave){
      w.blocked=false;
      w.obstructed=false;
    }
  }

  void drawsea(int seay){
    for(var b:Point.getrange(0,seay,SIZE,SIZE)) map[b.x][b.y].flooded=true;
    var crest=RPG.low(seay,SIZE);
    var wave=clear(crest,+1,-1,1,seay,SIZE);
    wave.addAll(clear(crest,-1,-1,1,seay,SIZE));
    for(var w:wave) w.flooded=false;
  }

  @Override
  public void generate(){
    var treeline=RPG.high(SIZE/2,SIZE-2);
    drawtreeline(treeline);
    drawsea(RPG.low(treeline,SIZE-1));
    rotate();
  }
}
