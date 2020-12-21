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

	static final FloorTile DEBUGROOM=null;
	static final FloorTile DEBUGCORRIDOR=null;

	/** Constructor. */
	public FloorTileTable(DungeonFloor f){
		var branch=f.dungeon.branches.stream().flatMap(b->b.tiles.stream())
				.collect(Collectors.toList());
		var rooms=selectrooms(branch);
		for(var r:rooms)
			add(r,10);
		if(RPG.chancein(2)&&DEBUGROOM==null) add(StaticTemplate.FACTORY,1);
		for(var c:selectcorridors(branch,rooms))
			add(c,10);
	}

	List<FloorTile> selectrooms(List<FloorTile> branch){
		if(Javelin.DEBUG&&DEBUGROOM!=null) return List.of(DEBUGROOM);
		branch=branch.stream().filter(t->!t.corridor).collect(Collectors.toList());
		if(!branch.isEmpty()) return branch;
		int nrooms=RPG.randomize(3,1,FloorTile.GENERATED.size());
		return RPG.pick(FloorTile.GENERATED,nrooms);
	}

	List<FloorTile> selectcorridors(List<FloorTile> branch,List<FloorTile> rooms){
		if(Javelin.DEBUG&&DEBUGCORRIDOR!=null) return List.of(DEBUGCORRIDOR);
		branch=branch.stream().filter(t->t.corridor).collect(Collectors.toList());
		if(!branch.isEmpty()) return branch;
		int ncorridors=0;
		var maxcorridors=Math.max(rooms.size(),FloorTile.CORRIDORS.size());
		while(RPG.chancein(2)&&ncorridors<maxcorridors)
			ncorridors+=1;
		return RPG.pick(FloorTile.CORRIDORS,ncorridors);
	}

	/** @return Selected templates. */
	public List<FloorTile> gettemplates(){
		return getrows().stream().map(r->(FloorTile)r).collect(Collectors.toList());
	}
}
