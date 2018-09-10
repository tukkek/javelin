package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.model.item.key.door.Key;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Brazier;
import javelin.model.world.location.dungeon.feature.Chest;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.FruitTree;
import javelin.model.world.location.dungeon.feature.Portal;
import javelin.model.world.location.dungeon.feature.Spirit;

/**
 * Generates a common {@link Dungeon} {@link Feature}.
 *
 * @author alex
 * @see FeatureRarityTable
 */
public class CommonFeatureTable extends Table {
	/** Default number of chances per {@link Feature}. */
	public static final int MAX = 10;

	public CommonFeatureTable() {
		add(Chest.class, CommonFeatureTable.MAX);
		add(Brazier.class, CommonFeatureTable.MAX);
		add(FruitTree.class, CommonFeatureTable.MAX);
		add(Portal.class, CommonFeatureTable.MAX);
		add(Spirit.class, CommonFeatureTable.MAX);
	}

	/**
	 * @param d
	 *            Active dungeon.
	 * @return <code>null</code> if an invalid feature has been rolled,
	 *         otherwise, a Feature that hasn't been positioned or placed yet.
	 */
	public Feature rollfeature(@SuppressWarnings("unused") Dungeon d) {
		Class<? extends Feature> type = (Class<? extends Feature>) roll();
		if (type.equals(Chest.class)) {
			return new Chest(-1, -1, Key.generate());
		}
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
