package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.controller.table.dungeon.InhabitantTable;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Campfire;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.Herb;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.Mirror;
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
		add(Herb.class,CommonFeatureTable.MAX);
		add(Inhabitant.class,CommonFeatureTable.MAX);
		add(Mirror.class,CommonFeatureTable.MAX);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Feature rollfeature(Dungeon d){
		Class<? extends Feature> type=(Class<? extends Feature>)roll();
		if(type.equals(Herb.class)&&d.level>Herb.MAXLEVEL) return null;
		if(type.equals(Inhabitant.class)){
			InhabitantTable npctable=d.tables.get(InhabitantTable.class);
			type=(Class<? extends Feature>)npctable.roll();
		}
		try{
			return type.getDeclaredConstructor().newInstance();
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}
}
