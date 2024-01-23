package javelin.controller.table.dungeon.feature;

import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;

/**
 * A table that contains either the Dungeon's {@link RareFeatureTable} or
 * {@link CommonFeatureTable}..
 *
 * @author alex
 */
public class FeatureRarityTable extends Table{
  /** Constructor. */
  public FeatureRarityTable(Class<? extends Table> common,
      Class<? extends Table> rare,DungeonFloor f){
    add(f.gettable(common),1,4);
    add(f.gettable(rare),2);
  }

  /** Constructor. */
  public FeatureRarityTable(DungeonFloor f){
    this(CommonFeatureTable.class,RareFeatureTable.class,f);
  }

  @Override
  public DungeonFeatureTable roll(){
    return (DungeonFeatureTable)super.roll();
  }
}
