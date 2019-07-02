package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.controller.table.dungeon.InhabitantTable;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Campfire;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.Mirror;
import javelin.model.world.location.dungeon.feature.Portal;
import javelin.model.world.location.dungeon.feature.Throne;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;

/**
 * Generates a rare {@link Dungeon} {@link Feature}, including
 * {@link Inhabitant}s.
 *
 * @author alex
 * @see InhabitantTable
 * @see FeatureRarityTable
 */
public class RareFeatureTable extends Table implements DungeonFeatureTable{
	public RareFeatureTable(){
		add(Fountain.class,CommonFeatureTable.MAX);
		add(Campfire.class,CommonFeatureTable.MAX);
		add(LearningStone.class,CommonFeatureTable.MAX);
		add(Mirror.class,CommonFeatureTable.MAX);
		add(Throne.class,CommonFeatureTable.MAX);
		add(Portal.class,CommonFeatureTable.MAX);
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
