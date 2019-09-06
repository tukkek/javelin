package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.controller.table.dungeon.InhabitantTable;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Brazier;
import javelin.model.world.location.dungeon.feature.Campfire;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.FruitTree;
import javelin.model.world.location.dungeon.feature.Herb;
import javelin.model.world.location.dungeon.feature.Passage;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;

/**
 * Generates a common {@link Dungeon} {@link Feature}.
 *
 * @author alex
 * @see FeatureRarityTable
 */
public class CommonFeatureTable extends Table implements DungeonFeatureTable{
	public CommonFeatureTable(){
		add(Brazier.class,ROWS);
		add(FruitTree.class,ROWS);
		add(Herb.class,ROWS);
		add(Inhabitant.class,ROWS);
		add(Passage.class,ROWS);
		add(Campfire.class,ROWS);
	}

	/**
	 * @param d Active dungeon.
	 * @return <code>null</code> if an invalid feature has been rolled, otherwise,
	 *         a Feature that hasn't been positioned or placed yet.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Feature rollfeature(@SuppressWarnings("unused") Dungeon d){
		var type=(Class<? extends Feature>)roll();
		if(type.equals(Herb.class)&&d.level>Herb.MAXLEVEL) return null;
		if(type.equals(Inhabitant.class)){
			var npctable=d.tables.get(InhabitantTable.class);
			type=(Class<? extends Feature>)npctable.roll();
		}
		try{
			return type.getDeclaredConstructor().newInstance();
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}
}
