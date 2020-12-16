package javelin.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import javelin.controller.generator.dungeon.template.MapTemplate;
import javelin.controller.table.dungeon.BranchTable;
import javelin.controller.template.Template;
import javelin.controller.terrain.Terrain;
import javelin.model.world.location.dungeon.branch.Branch;

/**
 * Helps quality-test {@link Branch}es (at a conceptual level, at least),
 * ensuring their important fields are properly used and enough fields being
 * used to make them interesting.
 *
 * Branches are designed to be starting points to developing more game content
 * as well, so this tool enables branches to be created with minimum effort at
 * first and then be slowly filled to their full extent, benefitting other
 * aspects of the game ({@link MapTemplate}s, {@link Template}s and more)...
 *
 * @author alex
 */
public class TestBranch{
	/** Single branch analysis. */
	public static boolean test(Branch b){
		if(b.terrains.isEmpty()){
			var noterrain="No terrain for %s. Prefer using all terrains to none.";
			System.out.println(String.format(noterrain,b));
			return false;
		}
		//TODO other fields
		return true;
	}

	/** {@link System#out}-based analysis test. */
	public static void test(){
		var byterrain=new HashMap<Terrain,Integer>();
		for(var t:Terrain.ALL)
			byterrain.put(t,0);
		for(var b:BranchTable.BRANCHES){
			if(!test(b)) return;
			for(var t:b.terrains)
				byterrain.put(t,byterrain.get(t)+1);
		}
		var terrains=new ArrayList<>(Arrays.asList(Terrain.ALL));
		terrains.sort((a,b)->-Integer.compare(byterrain.get(a),byterrain.get(b)));
		System.out.println("");
		System.out.println("Terrain round-up:");
		var count="  %s: %s branch(es)";
		var roundup=terrains.stream()
				.map(t->String.format(count,t,byterrain.get(t)))
				.collect(Collectors.joining("\n"));
		System.out.println(roundup);
	}
}
