/**
 *
 */
package javelin.controller.table.dungeon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.content.terrain.Desert;
import javelin.controller.content.terrain.Forest;
import javelin.controller.content.terrain.Terrain;
import javelin.controller.table.Table;
import javelin.model.world.World;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.branch.Branch;
import javelin.model.world.location.dungeon.branch.temple.AirTemple;
import javelin.model.world.location.dungeon.branch.temple.EarthTemple;
import javelin.model.world.location.dungeon.branch.temple.EvilTemple;
import javelin.model.world.location.dungeon.branch.temple.FireTemple;
import javelin.model.world.location.dungeon.branch.temple.GoodTemple;
import javelin.model.world.location.dungeon.branch.temple.MagicTemple;
import javelin.model.world.location.dungeon.branch.temple.WaterTemple;
import javelin.old.RPG;

/**
 * Based on {@link Terrain} - however this would seriously disadvantage some
 * branches ({@link Forest} versus {@link Desert}, for example, given their
 * prevalences in the {@link World} map). Aiming for a 50% chance of familiar
 * {@link Branch}es to exotic Branches seems ideal (1:1).
 *
 * @author alex
 */
public class BranchTable extends Table{
	/** All branches. */
	public static final List<Branch> BRANCHES=List.of(AirTemple.BRANCH,
			EarthTemple.BRANCH,EvilTemple.BRANCH,FireTemple.BRANCH,GoodTemple.BRANCH,
			MagicTemple.BRANCH,WaterTemple.BRANCH);

	/**
	 * Between familiar and exotic, there needs to be enough rows per
	 * {@link Dungeon} to generate enough variety in both prefix and suffix in
	 * {@link Dungeon#branches}. With 3, if either set is empty, there is enough
	 * variety (3 rows) for at least more than A+B and B+A permutations.
	 */
	static final int MINIMUM=3;

	/** {@link World} constructor. */
	public BranchTable(List<Terrain> terrains){
		var familiar=BRANCHES.stream()
				.filter(b->b.terrains.stream().anyMatch(t->terrains.contains(t)))
				.collect(Collectors.toList());
		var exotic=new ArrayList<>(BRANCHES);
		exotic.removeAll(familiar);
		var nbranches=Math.min(familiar.size(),exotic.size());
		if(nbranches<MINIMUM) nbranches=MINIMUM;
		register(familiar,nbranches);
		register(exotic,nbranches);
	}

	/** {@link Dungeon} constructor . */
	public BranchTable(DungeonFloor f){
		this(f.dungeon.terrains);
	}

	void register(List<Branch> candidates,int target){
		candidates=candidates.subList(0,Math.min(target,candidates.size()));
		for(var b:RPG.shuffle(candidates))
			add(b,1,1,0);
	}

	@Override
	public Branch roll(){
		return (Branch)super.roll();
	}

	/** @return Two different branches. */
	public List<Branch> rollaffixes(){
		var affixes=new HashSet<Branch>();
		while(affixes.size()<2)
			affixes.add(roll());
		return new ArrayList<>(affixes);
	}
}
