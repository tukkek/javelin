package javelin.controller.table.dungeon.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.feature.Altar;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.rare.CorruptedAltar;
import javelin.model.world.location.dungeon.feature.rare.DungeonMap;
import javelin.model.world.location.dungeon.feature.rare.Fountain;
import javelin.model.world.location.dungeon.feature.rare.FruitTree;
import javelin.model.world.location.dungeon.feature.rare.Herb;
import javelin.model.world.location.dungeon.feature.rare.LearningStone;
import javelin.model.world.location.dungeon.feature.rare.Mirror;
import javelin.model.world.location.dungeon.feature.rare.Spirit;
import javelin.model.world.location.dungeon.feature.rare.Throne;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Broker;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Hunter;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Inhabitant;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Leader;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Prisoner;
import javelin.model.world.location.dungeon.feature.rare.inhabitant.Trader;
import javelin.old.RPG;

/**
 * Generates a rare {@link DungeonFloor} {@link Feature}, including
 * {@link Inhabitant}s.
 *
 * Capping the amount of feature types to 1d4 per {@link DungeonTier}, to make
 * each dungeon more thematically cohesive rather than every dungeon being a
 * mish-mash of all feature types. This also generates interesting gameplay as a
 * player might want to go back and fully explore a Dungeon with lots of
 * {@link LearningStone}s or Recipes later on.
 *
 * @author alex
 * @see FeatureRarityTable
 */
public class RareFeatureTable extends Table implements DungeonFeatureTable{
  /** All features in this table. */
  public static final List<Class<? extends Feature>> ALL=new ArrayList<>();

  static final List<Class<? extends Feature>> LOTS=List.of(FruitTree.class,
      Herb.class);
  static final List<Class<? extends Feature>> SOME=List.of(Fountain.class,
      LearningStone.class,Spirit.class,Broker.class,Prisoner.class,Trader.class,
      Hunter.class);
  static final List<Class<? extends Feature>> FEW=List.of(Mirror.class,
      Throne.class,Leader.class,DungeonMap.class,CorruptedAltar.class,
      Altar.class);
  static final Class<? extends Feature> DEBUG=null;

  static{
    ALL.addAll(LOTS);
    ALL.addAll(SOME);
    ALL.addAll(FEW);
  }

  /** Constructor. */
  public RareFeatureTable(DungeonFloor f){
    if(Javelin.DEBUG&&DEBUG!=null) add(DEBUG,1);
    else generate(f);
  }

  /** @see CommonFeatureTable#generate() */
  public void generate(DungeonFloor f){
    var tier=f.gettier().tier.getordinal();
    var features=define(tier,f);
    var types=RPG.shuffle(new ArrayList<>(features.keySet()));
    var ntypes=RPG.randomize(RPG.rolldice(tier+1,4),2,types.size());
    for(var t:types.subList(0,ntypes)) add(t,features.get(t));
  }

  HashMap<Class<? extends Feature>,Integer> define(int tier,DungeonFloor floor){
    var features=new HashMap<Class<? extends Feature>,Integer>();
    for(var f:LOTS) features.put(f,ROWS*2);
    if(tier>=1) for(var f:SOME) features.put(f,ROWS);
    if(tier>=2) for(var f:FEW) features.put(f,ROWS/2);
    return features;
  }

  @Override
  public Feature rollfeature(DungeonFloor d){
    return CommonFeatureTable.generate(this,d);
  }
}
