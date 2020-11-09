package javelin.controller.table.dungeon.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.TieredList;
import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.feature.Furniture;
import javelin.old.RPG;
import javelin.test.TestFurniture;

/**
 * @see Furniture
 * @author alex
 */
public class FurnitureTable extends Table{
	/** @see TestFurniture */
	public static class Type extends Table{
		String name;
		DungeonTier rarity;

		Type(String name,int nimages,DungeonTier rarity){
			this.name=name;
			this.rarity=rarity;
			for(var i=1;i<=nimages;i++)
				add(name+i,1);
		}

		@Override
		public int hashCode(){
			return name.hashCode();
		}

		@Override
		public boolean equals(Object o){
			var t=o instanceof Type?(Type)o:null;
			return t!=null&&name.equals(t.name);
		}
	}

	static final Type COBWEB=new Type("cobweb",20,DungeonTier.CAVE);
	static final Type MISC=new Type("misc",39,DungeonTier.CAVE);
	static final Type PLANT=new Type("plant",101,DungeonTier.CAVE);
	static final Type ROCK=new Type("rock",31,DungeonTier.CAVE);
	static final Type BLOOD=new Type("blood",122,DungeonTier.DUNGEON);
	static final Type STATUE=new Type("statue",49,DungeonTier.DUNGEON);
	static final Type FURNITURE=new Type("furniture",17,DungeonTier.KEEP);
	/** All available categories. */
	public static final List<Type> CATEGORIES=List.of(STATUE,FURNITURE,MISC,
			COBWEB,BLOOD,PLANT,ROCK);

	static final int TYPES=3;
	static final int AMOUNTPERTYPE=3;

	/** Constructor. */
	public FurnitureTable(DungeonFloor f){
		var tiered=new TieredList<Type>(f.gettier());
		for(var c:CATEGORIES)
			tiered.addtiered(c,c.rarity);
		var types=new ArrayList<>(
				tiered.stream().distinct().collect(Collectors.toList()));
		var ntypes=Math.min(types.size(),RPG.randomize(TYPES,1,Integer.MAX_VALUE));
		for(var t:RPG.shuffle(types).subList(0,ntypes)){
			var amount=RPG.randomize(2,1,Integer.MAX_VALUE);
			for(var i=0;i<amount;i++)
				add(t.roll(),1);
		}
	}

	/** @return Total number of {@link Furniture} images. */
	public static int count(){
		var c=0;
		for(var category:CATEGORIES)
			c+=category.getrows().size();
		c+=1;//christmas easter egg
		c+=1;//halloween easter egg
		return c;
	}
}
