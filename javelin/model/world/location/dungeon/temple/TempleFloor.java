package javelin.model.world.location.dungeon.temple;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
			CommonFeatureTable table=tables.get(CommonFeatureTable.class);
			int size=table.getchances();
			table.add(Fountain.class,size);
			if(temple.feature!=null) table.add(temple.feature,size);
		}
		super.generatefeatures(nfeatures,zoner);
	}

	@Override
	public boolean hazard(){
		return temple.hazard(this);
	}

	@Override
	protected Combatants generateencounter(int level,List<Terrain> terrains){
		Combatants encounter=null;
		while(encounter==null)
			encounter=super.generateencounter(level-RPG.r(1,4),terrains);
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
