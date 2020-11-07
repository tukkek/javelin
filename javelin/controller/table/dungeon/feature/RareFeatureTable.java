package javelin.controller.table.dungeon.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javelin.Javelin;
import javelin.controller.table.Table;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonTier;
import javelin.model.world.location.dungeon.Wilderness;
import javelin.model.world.location.dungeon.feature.DungeonMap;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.FruitTree;
import javelin.model.world.location.dungeon.feature.Herb;
import javelin.model.world.location.dungeon.feature.LearningStone;
import javelin.model.world.location.dungeon.feature.Mirror;
import javelin.model.world.location.dungeon.feature.Recipe;
import javelin.model.world.location.dungeon.feature.Spirit;
import javelin.model.world.location.dungeon.feature.Throne;
import javelin.model.world.location.dungeon.feature.inhabitant.Broker;
import javelin.model.world.location.dungeon.feature.inhabitant.Inhabitant;
import javelin.model.world.location.dungeon.feature.inhabitant.Leader;
import javelin.model.world.location.dungeon.feature.inhabitant.Prisoner;
import javelin.model.world.location.dungeon.feature.inhabitant.Trader;
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
 * @see InhabitantTable
 * @see FeatureRarityTable
 */
public class RareFeatureTable extends Table implements DungeonFeatureTable{
	/** All features in this table. */
	public static final List<Class<? extends Feature>> ALL=new ArrayList<>();

	static final Class<? extends Feature> DEBUG=null;
	static final List<Class<? extends Feature>> COMMON=List.of(FruitTree.class,
			Herb.class);
	static final List<Class<? extends Feature>> AVERAGE=List.of(Fountain.class,
			LearningStone.class,Spirit.class,Broker.class,Prisoner.class,Trader.class,
			Recipe.class);
	static final List<Class<? extends Feature>> RARE=List.of(Mirror.class,
			Throne.class,Leader.class,DungeonMap.class);

	static{
		ALL.addAll(COMMON);
		ALL.addAll(AVERAGE);
		ALL.addAll(RARE);
	}

	/** Constructor. */
	public RareFeatureTable(DungeonFloor f){
		super();
		if(Javelin.DEBUG&&DEBUG!=null){
			add(DEBUG,1);
			return;
		}
		var tier=f.gettier().tier.getordinal();
		var features=define(tier,f);
		var types=RPG.shuffle(new ArrayList<>(features.keySet()));
		var ntypes=RPG.randomize(RPG.rolldice(tier+1,4),2,types.size());
		for(var t:types.subList(0,ntypes))
			add(t,features.get(t));
	}

	HashMap<Class<? extends Feature>,Integer> define(int tier,DungeonFloor floor){
		var features=new HashMap<Class<? extends Feature>,Integer>();
		for(var f:COMMON)
			features.put(f,ROWS*2);
		if(tier>=1) for(var f:AVERAGE)
			features.put(f,ROWS);
		if(tier>=2) for(var f:RARE)
			features.put(f,ROWS/2);
		if(floor.dungeon instanceof Wilderness) for(var f:Wilderness.FORBIDDEN)
			features.remove(f);
		return features;
	}

	@Override
	public Feature rollfeature(DungeonFloor d){
		return CommonFeatureTable.generate(this,d);
	}
}
