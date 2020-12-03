package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.feature.Brazier;
import javelin.model.world.location.dungeon.feature.Campfire;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.LoreNote;
import javelin.model.world.location.dungeon.feature.Passage;

/**
 * Generates a common {@link DungeonFloor} {@link Feature}. Unlike with the
 * {@link RareFeatureTable}, any of these features may appear in any Dungeon.
 *
 * @author alex
 * @see FeatureRarityTable
 */
public class CommonFeatureTable extends Table implements DungeonFeatureTable{
	/** Constructor. */
	public CommonFeatureTable(DungeonFloor floor){
		add(Passage.class,1);
		add(Brazier.class,1);
		add(LoreNote.class,2);
		add(Campfire.class,getchances());
		var c=getchances();
		floor.dungeon.branches.stream().flatMap(b->b.features.stream()).forEach(f->{
			add(f,c);
		});
	}

	/**
	 * @param d Active dungeon.
	 * @return <code>null</code> if an invalid feature has been rolled, otherwise,
	 *         a Feature that hasn't been positioned or placed yet.
	 */
	@Override
	public Feature rollfeature(DungeonFloor d){
		return generate(this,d);
	}

	/**
	 * @param t {@link CommonFeatureTable} or {@link RareFeatureTable}.
	 * @param df {@link Dungeon#active} (probably geing generated).
	 * @return A valid, non-<code>null</code> feature.
	 * @see Feature#validate(DungeonFloor)
	 */
	@SuppressWarnings("unchecked")
	public static Feature generate(Table t,DungeonFloor df){
		Feature f=null;
		while(f==null)
			try{
				var type=(Class<? extends Feature>)t.roll();
				f=type.getDeclaredConstructor(DungeonFloor.class).newInstance(df);
				if(!f.validate(df)) f=null;
			}catch(ReflectiveOperationException e){
				throw new RuntimeException(e);
			}
		return f;
	}
}
