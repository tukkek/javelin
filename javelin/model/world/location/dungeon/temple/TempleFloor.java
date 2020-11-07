package javelin.model.world.location.dungeon.temple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.kit.Kit;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.controller.terrain.Terrain;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.Furniture;
import javelin.model.world.location.dungeon.feature.chest.ArtifactChest;
import javelin.old.RPG;

/**
 * Unlike normal dungeons {@link Temple}s have many floors (levels), chests with
 * rubies and an Altar on the deepest level.
 *
 * @author alex
 */
public class TempleFloor extends DungeonFloor{
	public Temple temple;

	/** Constructor. */
	public TempleFloor(Integer level,Dungeon d){
		super(level,d);
	}

	@Override
	protected Feature generatespecialchest(){
		return dungeon.floors.indexOf(this)==dungeon.floors.size()-1
				?new ArtifactChest(temple.artifact)
				:super.generatespecialchest();
	}

	@Override
	protected void generatefeatures(int nfeatures,DungeonZoner zoner){
		if(this==temple.floors.get(0)){
			var t=gettable(CommonFeatureTable.class);
			var c=t.getchances();
			t.add(Fountain.class,c);
			if(temple.feature!=null) t.add(temple.feature,c);
		}
		super.generatefeatures(nfeatures,zoner);
	}

	@Override
	public boolean hazard(){
		return temple.hazard(this);
	}

	@Override
	protected Combatants generateencounter(int level,List<Terrain> terrains){
		Combatants encounter=super.generateencounter(level-RPG.r(1,4),terrains);
		if(encounter==null){
			if(!Javelin.DEBUG) return null;
			var error="Cannot create encounter for level %s %s.";
			throw new RuntimeException(String.format(error,level,this));
		}
		if(!temple.validate(encounter.getmonsters())) return null;
		var kits=new HashMap<Combatant,List<Kit>>(encounter.size());
		for(var c:encounter)
			kits.put(c,Kit.getpreferred(c.source,true));
		while(ChallengeCalculator.calculateel(encounter)<level){
			var weakest=encounter.getweakest();
			RPG.pick(kits.get(weakest)).upgrade(weakest);
		}
		return encounter;
	}

	@Override
	protected LinkedList<Furniture> generatefurniture(int minimum){
		return null;
	}
}
