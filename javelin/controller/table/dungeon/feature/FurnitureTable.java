package javelin.controller.table.dungeon.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javelin.controller.TieredList;
import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.feature.Furniture;
import javelin.old.RPG;

/**
 * @see Furniture
 * @author alex
 */
public class FurnitureTable extends Table{
	static class Type extends Table{
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

	static final int TYPES=2;
	static final int AMOUNTPERTYPE=2;
	static final Type STATUE=new Type("statue",49,DungeonTier.DUNGEON);
	/** Actual furniture like wardrobes, drawers, tables... */
	static final Type FURNITURE=new Type("furniture",17,DungeonTier.KEEP);
	static final Type MISC=new Type("misc",9,DungeonTier.CAVE);
	static final List<Type> ALL=List.of(STATUE,FURNITURE,MISC);

	/** Constructor. */
	public FurnitureTable(){
		var tiered=new TieredList<Type>(Dungeon.active.gettier());
		for(var type:ALL)
			tiered.addtiered(type,type.rarity);
		var types=new ArrayList<>(
				tiered.stream().distinct().collect(Collectors.toList()));
		var ntypes=Math.min(types.size(),RPG.randomize(TYPES,1,Integer.MAX_VALUE));
		for(var t:RPG.shuffle(types).subList(0,ntypes)){
			var amount=RPG.randomize(2,1,Integer.MAX_VALUE);
			for(var i=0;i<amount;i++)
				add(t.roll(),1);
		}
	}
}