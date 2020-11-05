package javelin.controller.table.dungeon;

import java.util.ArrayList;
import java.util.List;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.chest.ArmorDisplay;
import javelin.model.world.location.dungeon.feature.chest.ArtDisplay;
import javelin.model.world.location.dungeon.feature.chest.DeadAdventurer;
import javelin.model.world.location.dungeon.feature.chest.Bookcase;
import javelin.model.world.location.dungeon.feature.chest.Chest;
import javelin.model.world.location.dungeon.feature.chest.DeadMage;
import javelin.model.world.location.dungeon.feature.chest.GemDisplay;
import javelin.model.world.location.dungeon.feature.chest.VestDisplay;
import javelin.old.RPG;

/**
 * All specialized types of {@link Chest}s for a dungeon.
 *
 * @author alex
 */
public class ChestTable extends Table{
	static final List<Class<? extends Chest>> TYPES=new ArrayList<>(
			List.of(ArmorDisplay.class,ArtDisplay.class,DeadAdventurer.class,Bookcase.class,
					DeadMage.class,GemDisplay.class,VestDisplay.class));

	/** Constructor. */
	public ChestTable(){
		var tier=Dungeon.active.gettier().tier.getordinal();
		var nhighlights=RPG.randomize(tier+1,1,TYPES.size()-1);
		var highlights=TYPES.subList(0,nhighlights);
		for(var t:TYPES)
			add(t,highlights.contains(t)?TYPES.size():1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Chest> roll(){
		return (Class<? extends Chest>)super.roll();
	}
}
