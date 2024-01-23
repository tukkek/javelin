package javelin.controller.table.dungeon.feature;

import javelin.model.world.location.academy.Guild;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.common.Campfire;
import javelin.model.world.location.dungeon.feature.rare.FruitTree;
import javelin.model.world.location.dungeon.feature.rare.Herb;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Hunter;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Trader;
import javelin.model.world.location.town.Town;
import javelin.model.world.location.town.labor.basic.Lodge;

/**
 * {@link Wilderness} are the more casual mode of play, similar to early
 * role-playing video-games - where simply exploring and fighting was the core
 * gameplay. The limited {@link Feature}-set also helps simplify them.
 *
 * {@link Campfire}s and {@link FruitTree}s provide rest and healing.
 * {@link Hunter}s provide aid for harder {@link DungeonFloor#encounters} and
 * make farming more convenient. {@link Trader}s provide the opportunity to
 * exchange farmed resources for progression.
 *
 * Players are still expected to go back to {@link Town} to do downtime activity
 * like using {@link Lodge}s and {@link Guild}s.
 */
public class WildernessFeatures extends FeatureRarityTable{
  /** Common {@link Wilderness} {@link Feature}s. */
  public static class Common extends CommonFeatureTable{
    /** Constructor. */
    public Common(DungeonFloor f){
      super(f);
    }

    @Override
    public void generate(){
      add(Campfire.class,3);
      add(Hunter.class,2);
    }
  }

  /** Rare {@link Wilderness} {@link Feature}s. */
  public static class Rare extends CommonFeatureTable{
    /** Constructor. */
    public Rare(DungeonFloor f){
      super(f);
    }

    @Override
    public void generate(){
      add(Trader.class);
      add(FruitTree.class);
      add(Herb.class);
    }
  }

  /** Constructor. */
  public WildernessFeatures(DungeonFloor f){
    super(Common.class,Rare.class,f);
  }
}
