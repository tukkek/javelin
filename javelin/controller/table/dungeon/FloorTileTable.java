package javelin.controller.table.dungeon;

import java.util.List;
import java.util.stream.Collectors;

import javelin.Javelin;
import javelin.controller.generator.dungeon.DungeonGenerator;
import javelin.controller.generator.dungeon.template.FloorTile;
import javelin.controller.generator.dungeon.template.StaticTemplate;
import javelin.controller.generator.dungeon.template.mutator.Mutator;
import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.old.RPG;

/** @see DungeonGenerator */
public class FloorTileTable extends Table{
  public static final Mutator DEBUGMUTATOR=null;

  static final FloorTile DEBUGCORRIDOR=null;
  static final FloorTile DEBUGROOM=null;

  /** Constructor. */
  public FloorTileTable(DungeonFloor f){
    var branch=f.dungeon.branches.stream().flatMap(b->b.tiles.stream())
        .collect(Collectors.toList());
    var rooms=selectrooms(branch);
    for(var r:rooms) add(r,10);
    if(StaticTemplate.ENABLED&&RPG.chancein(2)&&DEBUGROOM==null)
      add(StaticTemplate.FACTORY,1);
    for(var c:selectcorridors(branch,rooms)) add(c,10);
  }

  List<FloorTile> selectrooms(List<FloorTile> branch){
    if(Javelin.DEBUG&&DEBUGROOM!=null) return List.of(DEBUGROOM);
    var nrooms=RPG.randomize(4,1,FloorTile.GENERATED.size());
    var rooms=RPG.pick(FloorTile.GENERATED,nrooms);
    rooms.addAll(branch.stream().filter(t->!t.corridor).toList());
    return rooms;
  }

  List<FloorTile> selectcorridors(List<FloorTile> branch,List<FloorTile> rooms){
    if(Javelin.DEBUG&&DEBUGCORRIDOR!=null) return List.of(DEBUGCORRIDOR);
    var ncorridors=0;
    var maxcorridors=Math.max(rooms.size(),FloorTile.CORRIDORS.size());
    while(RPG.chancein(2)&&ncorridors<maxcorridors) ncorridors+=1;
    var corridors=RPG.pick(FloorTile.CORRIDORS,ncorridors);
    corridors.addAll(branch.stream().filter(t->t.corridor).toList());
    return corridors;
  }

  /** @return Selected templates. */
  public List<FloorTile> gettemplates(){
    return getrows().stream().map(r->(FloorTile)r).collect(Collectors.toList());
  }
}
