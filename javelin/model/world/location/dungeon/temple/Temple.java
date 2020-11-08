package javelin.model.world.location.dungeon.temple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javelin.Javelin;
import javelin.controller.challenge.ChallengeCalculator;
import javelin.controller.generator.NpcGenerator;
import javelin.controller.generator.feature.LocationGenerator;
import javelin.controller.kit.Kit;
import javelin.controller.table.dungeon.feature.CommonFeatureTable;
import javelin.controller.terrain.Terrain;
import javelin.controller.terrain.hazard.Hazard;
import javelin.controller.wish.Win;
import javelin.controller.wish.Wish;
import javelin.model.Realm;
import javelin.model.item.Tier;
import javelin.model.item.artifact.Artifact;
import javelin.model.unit.Combatant;
import javelin.model.unit.Combatants;
import javelin.model.unit.Monster;
import javelin.model.world.World;
import javelin.model.world.location.Location;
import javelin.model.world.location.dungeon.Dungeon;
import javelin.model.world.location.dungeon.DungeonEntrance;
import javelin.model.world.location.dungeon.DungeonFloor;
import javelin.model.world.location.dungeon.DungeonZoner;
import javelin.model.world.location.dungeon.feature.Feature;
import javelin.model.world.location.dungeon.feature.Fountain;
import javelin.model.world.location.dungeon.feature.Furniture;
import javelin.model.world.location.dungeon.feature.chest.ArtifactChest;
import javelin.model.world.location.dungeon.temple.WaterTemple.WaterTempleEntrance;
import javelin.old.RPG;

/**
 * Temples are Javelin's {@link Tier#EPIC} {@link Dungeon}s, with features that
 * distinguish them from ordinary {@link Dungeon}s like {@link Hazard}s and
 * {@link NpcGenerator}-based {@link Combatant}s from
 * non-{@link Terrain#UNDERGROUND} pools.
 *
 * Deep in the Temple there will be an {@link Artifact} and once all of those
 * are collected the player can make the {@link Win} {@link Wish}.
 *
 * @author alex
 */
public abstract class Temple extends Dungeon{
	/** @see Location */
	public static class TempleEntrance extends DungeonEntrance{
		/** Constructor. */
		public TempleEntrance(Temple t){
			super(t);
		}

		@Override
		protected void generate(){
			var t=(Temple)dungeon;
			while(x==-1||!t.terrains.contains(Terrain.get(x,y)))
				super.generate();
		}
	}

	class TempleFloor extends DungeonFloor{
		TempleFloor(Integer level,Dungeon d){
			super(level,d);
		}

		@Override
		protected Feature generatespecialchest(){
			return this==dungeon.floors.getLast()?new ArtifactChest(artifact)
					:super.generatespecialchest();
		}

		@Override
		protected void generatefeatures(int nfeatures,DungeonZoner zoner){
			//TODO is this really needed? is Campfire better?
			if(this==floors.getFirst()){
				var t=gettable(CommonFeatureTable.class);
				var c=t.getchances();
				t.add(Fountain.class,c);
				if(feature!=null) t.add(feature,c);
			}
			super.generatefeatures(nfeatures,zoner);
		}

		@Override
		public boolean hazard(){
			return Temple.this.hazard(this);
		}

		@Override
		protected Combatants generateencounter(int level,List<Terrain> terrains){
			var combatants=super.generateencounter(level-RPG.r(1,4),terrains);
			if(combatants==null){
				if(!Javelin.DEBUG) return null;
				var error="Cannot create encounter for level %s %s.";
				throw new RuntimeException(String.format(error,level,this));
			}
			if(!validate(combatants.getmonsters())) return null;
			var kits=new HashMap<Combatant,List<Kit>>(combatants.size());
			for(var c:combatants)
				kits.put(c,Kit.getpreferred(c.source,true));
			while(ChallengeCalculator.calculateel(combatants)<level){
				var weakest=combatants.getweakest();
				RPG.pick(kits.get(weakest)).upgrade(weakest);
			}
			return combatants;
		}

		@Override
		protected LinkedList<Furniture> generatefurniture(int minimum){
			return null;
		}
	}

	/** @see LocationGenerator */
	public static void generatetemples(){
		var w=new WaterTemple(Tier.EPIC.getrandomel(false));
		new WaterTempleEntrance(w).place();
		for(var type:List.of(AirTemple.class,EarthTemple.class,FireTemple.class,
				EvilTemple.class,GoodTemple.class,MagicTemple.class))
			try{
				var el=Tier.EPIC.getrandomel(false);
				var t=type.getConstructor(Integer.class).newInstance(el);
				new TempleEntrance(t).place();
			}catch(ReflectiveOperationException e){
				throw new RuntimeException(e);
			}
	}

	/**
	 * Reward found on the deepest of the {@link #floors}.
	 *
	 * @see ArtifactChest
	 */
	public Artifact artifact;
	/** {@link DungeonFloor} {@link Feature} most likely to be found here. */
	public Class<? extends Feature> feature=null;

	Realm realm;

	/** Constructor. */
	public Temple(Realm r,List<Terrain> t,int level,Artifact a,String fluffp){
		super("The Temple of "+r.getname(),level,
				RPG.randomize(2,1,Integer.MAX_VALUE));
		artifact=a;
		fluff=fluffp;
		realm=r;
		terrains.addAll(t);
	}

	/** Constructor with a single {@link Terrain}. */
	public Temple(Realm r,Terrain t,int level,Artifact a,String fluff){
		this(r,List.of(t),level,a,fluff);
	}

	@Override
	protected DungeonFloor createfloor(int level){
		return new TempleFloor(level,this);
	}

	@Override
	public String getimagename(){
		return "temple"+realm.getname().toLowerCase();
	}

	@Override
	public boolean validate(List<Monster> foes){
		return true;
	}

	/**
	 * @return <code>true</code> if a hazard happens.
	 * @see DungeonFloor#hazard()
	 */
	public boolean hazard(DungeonFloor f){
		return false;
	}

	/** @return All temple {@link Location}s. */
	public static List<TempleEntrance> gettemples(){
		var temples=new ArrayList<TempleEntrance>(7);
		for(var a:World.getactors())
			if(a instanceof TempleEntrance) temples.add((TempleEntrance)a);
		return temples;
	}
}
