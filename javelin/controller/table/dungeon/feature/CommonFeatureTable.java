package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.controller.table.dungeon.InhabitantTable;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Brazier;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.FruitTree;
import javelin.model.world.location.dungeon.feature.Herb;
import javelin.model.world.location.dungeon.feature.Spirit;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;

/**
 * Generates a common {@link Dungeon} {@link Feature}.
 *
 * @author alex
 * @see FeatureRarityTable
 */
public class CommonFeatureTable extends Table implements DungeonFeatureTable{
	/** Default number of chances per {@link Feature}. */
	public static final int MAX=10;

	public CommonFeatureTable(){
		add(Brazier.class,CommonFeatureTable.MAX);
		add(FruitTree.class,CommonFeatureTable.MAX);
		add(Spirit.class,CommonFeatureTable.MAX);
		add(Herb.class,CommonFeatureTable.MAX);
		add(Inhabitant.class,CommonFeatureTable.MAX);
	}

	/**
	 * @param d Active dungeon.
	 * @return <code>null</code> if an invalid feature has been rolled, otherwise,
	 *         a Feature that hasn't been positioned or placed yet.
	 */
	@Override
	public Feature rollfeature(@SuppressWarnings("unused") Dungeon d){
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
