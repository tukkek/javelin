package javelin.controller.table.dungeon;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.feature.chest.ArtDisplay;
import javelin.model.world.location.dungeon.feature.chest.Backpack;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.chest.DeadAdventurer;
import javelin.model.world.location.dungeon.feature.chest.DeadFighter;
import javelin.model.world.location.dungeon.feature.chest.DeadMage;
import javelin.model.world.location.dungeon.feature.chest.GemDisplay;

/**
 * All specialized types of {@link Chest}s for a dungeon.
 *
 * @author alex
 */
public class ChestTable extends Table{
	static final List<Class<? extends Chest>> TYPES=new ArrayList<>(
			List.of(ArtDisplay.class,Backpack.class,DeadAdventurer.class,
					DeadFighter.class,DeadMage.class,GemDisplay.class));

	/** Constructor. */
	public ChestTable(){
		//		var ntypes=RPG.randomize(3,1,TYPES.size()-1);
		//		for(var type:RPG.shuffle(TYPES).subList(0,ntypes))
		//			add(type,10);
		for(var type:TYPES)
			add(type,10);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Chest> roll(){
		return (Class<? extends Chest>)super.roll();
	}
}
