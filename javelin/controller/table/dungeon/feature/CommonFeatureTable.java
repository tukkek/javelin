package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.feature.Brazier;
import javelin.model.world.location.dungeon.feature.Campfire;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.LoreNote;
import javelin.model.world.location.dungeon.feature.Passage;

/**
 * Generates a common {@link Dungeon} {@link Feature}. Unlike with the
 * {@link RareFeatureTable}, any of these features may appear in any Dungeon.
 *
 * @author alex
 * @see FeatureRarityTable
 */
public class CommonFeatureTable extends Table implements DungeonFeatureTable{
	/** Constructor. */
	public CommonFeatureTable(){
		add(Passage.class,1);
		add(Brazier.class,1);
		add(LoreNote.class,2);
		add(Campfire.class,getchances());
	}

	/**
	 * @param d Active dungeon.
	 * @return <code>null</code> if an invalid feature has been rolled, otherwise,
	 *         a Feature that hasn't been positioned or placed yet.
	 */
	@Override
	public Feature rollfeature(Dungeon d){
		return generate(this,d);
	}

	/**
	 * @param t {@link CommonFeatureTable} or {@link RareFeatureTable}.
	 * @param d {@link Dungeon#active} (probably geing generated).
	 * @return A valid, non-<code>null</code> feature.
	 * @see Feature#validate()
	 */
	@SuppressWarnings("unchecked")
	public static Feature generate(Table t,Dungeon d){
		try{
			Feature f=null;
			while(f==null){
				var type=(Class<? extends Feature>)t.roll();
				f=type.getDeclaredConstructor().newInstance();
				if(!f.validate()) f=null;
			}
			return f;
		}catch(ReflectiveOperationException e){
			throw new RuntimeException(e);
		}
	}
}
