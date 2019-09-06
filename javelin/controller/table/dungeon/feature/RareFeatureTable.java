package javelin.controller.table.dungeon.feature;

import java.util.List;

import javelin.controller.table.Table;
import javelin.controller.table.dungeon.InhabitantTable;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.DungeonMap;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.Mirror;
import javelin.model.world.location.dungeon.feature.Portal;
import javelin.model.world.location.dungeon.feature.Spirit;
import javelin.model.world.location.dungeon.feature.Throne;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;

/**
 * Generates a rare {@link Dungeon} {@link Feature}, including
 * {@link Inhabitant}s.
 *
 * TODO map feature, which shows the entire map (or a big portion of it)
 *
 * @author alex
 * @see InhabitantTable
 * @see FeatureRarityTable
 */
public class RareFeatureTable extends Table implements DungeonFeatureTable{
	/** Constructor. */
	public RareFeatureTable(){
		for(var f:List.of(Fountain.class,LearningStone.class,Mirror.class,
				Throne.class,Portal.class,Spirit.class,DungeonMap.class))
			add(f,ROWS);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Feature rollfeature(Dungeon d){
		try{
			var type=(Class<? extends Feature>)roll();
			return type.getDeclaredConstructor().newInstance();
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}
}
