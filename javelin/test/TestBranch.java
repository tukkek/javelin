package javelin.test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javelin.controller.ContentSummary;
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
 * first and then be slowly filled in to their full extent, benefitting other
 * aspects of the game ({@link MapTemplate}s, {@link Template}s and more)...
 *
 * @see ContentSummary
 * @author alex
 */
public class TestBranch{
	static String BRANCHREPORT="  %s (missing %s fields): %s.";
	static String NOTERRAIN="No terrain for branch %s! Prefer using all terrains to none.";

	Map<Branch,List<String>> report=new HashMap<>(BranchTable.BRANCHES.size());
	PrintStream out;

	/** Constructor. */
	public TestBranch(PrintStream s){
		out=s;
	}

	/** {@link System#out} constructor. */
	public TestBranch(){
		this(System.out);
	}

	/** Single branch analysis. */
	public void test(Branch b){
		if(b.terrains.isEmpty())
			throw new RuntimeException(String.format(NOTERRAIN,b));
		var missing=new ArrayList<String>();
		if(b.features.isEmpty()) missing.add("features");
		if(b.hazards.isEmpty()) missing.add("hazards");
		if(b.mutators.isEmpty()) missing.add("mutators");
		if(b.templates.isEmpty()) missing.add("templates");
		if(b.tiles.isEmpty()) missing.add("tiles");
		if(b.treasure.isEmpty()) missing.add("treasure");
		report.put(b,missing);
	}

	void reportbyterrain(){
		var byterrain=new HashMap<Terrain,Integer>();
		var terrains=new ArrayList<>(Arrays.asList(Terrain.ALL));
		for(var t:terrains)
			byterrain.put(t,0);
		for(var b:BranchTable.BRANCHES)
			for(var t:b.terrains)
				byterrain.put(t,byterrain.get(t)+1);
		terrains.sort((a,b)->-Integer.compare(byterrain.get(a),byterrain.get(b)));
		out.println("Branches by terrain");
		var count="  %s: %s branch(es)";
		var roundup=terrains.stream()
				.map(t->String.format(count,t,byterrain.get(t)))
				.collect(Collectors.joining("\n"));
		out.println(roundup);
		out.println();
	}

	/** {@link System#out}-based analysis test. */
	public void test(){
		var branches=new ArrayList<>(BranchTable.BRANCHES);
		for(var b:branches)
			test(b);
		branches.sort(
				(a,b)->-Integer.compare(report.get(a).size(),report.get(b).size()));
		out.println("Branches");
		for(var b:branches){
			var missing=report.get(b);
			var fields=String.join(", ",missing);
			out.println(String.format(BRANCHREPORT,b,missing.size(),fields));
		}
		out.println();
		reportbyterrain();
	}
}
